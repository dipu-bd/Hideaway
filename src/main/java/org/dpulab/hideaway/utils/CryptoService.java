/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author dipu
 */
public class CryptoService {
    
    private static final CryptoService INSTANCE = new CryptoService();
    
    public static CryptoService getDefault() {
        return CryptoService.INSTANCE;
    }
    
    private CryptoService() {
        // Security.addProvider(new BouncyCastlePQCProvider());        
    }
    
    /**
     * Get the hash of given text using SHA-256 algorithm.
     * @param text The input text.
     * @return The hash of the text.
     */
    public String getHash(final String text) {
        String hash = text;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(text.getBytes("UTF-8"));
            byte[] digest = sha256.digest();
            hash = Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(CryptoService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hash;
    }
    
    protected String getModifierPassword(String password) {
        StringBuilder sb = new StringBuilder();
        sb.append(password);
        sb.reverse();
        sb.append(password.substring(password.length() / 2));
        sb.reverse();
        sb.append(password.substring(0, password.length() / 2));
        sb.reverse();
        sb.append("0102030405060708");
        return sb.toString();
    }
    
    public SecretKeySpec generateKeySpec(String password, String algorithm) throws UnsupportedEncodingException {
        byte[] key = password.getBytes("UTF-8");
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        SecretKeySpec spec = new SecretKeySpec(key, algorithm);
        return spec;
    }
    
    /***
     * Decrypt a cipher text encrypted with AES.
     * @param cipherText The bytes to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public byte[] decryptAES(final byte[] cipherText, String password) throws GeneralSecurityException, IOException {
        // Generate AES key spec
        password = this.getModifierPassword(password);
        SecretKeySpec key = generateKeySpec(password, "AES");
        
        // Decrypt using AES
        Cipher decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec("Sudipto Chandra*".getBytes());
        decryptor.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = decryptor.doFinal(cipherText);
        
        return plainText;
    }
    
    /**
     * Decrypt a file encrypted with AES and returns the plain text content.
     * @param cipherFile The file to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public byte[]  decryptAES(final File cipherFile, String password) throws GeneralSecurityException, IOException {
        byte[] content = FileUtils.readFileToByteArray(cipherFile);
        return this.decryptAES(content, password);
    }
}
