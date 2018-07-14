/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

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
import javax.crypto.NoSuchPaddingException;
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
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(text.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            hash = Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(CryptoService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hash;
    }
    
    /***
     * Decrypt a cipher text encrypted with AES.
     * @param cipherText The bytes to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public String decryptAES(final byte[] cipherText, String password) throws GeneralSecurityException, IOException {
        byte[] key = password.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        
        Cipher decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec("Sudipto Chandra*".getBytes());
        decryptor.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] plainText = decryptor.doFinal(cipherText);
        
        return new String(plainText, "UTF-8");
    }
    
    /**
     * Decrypt a file encrypted with AES and returns the plain text content.
     * @param cipherFile The file to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException
     * @throws IOException 
     */
    public String decryptAES(final File cipherFile, String password) throws GeneralSecurityException, IOException {
        byte[] content = FileUtils.readFileToByteArray(cipherFile);
        return this.decryptAES(content, password);
    }
}
