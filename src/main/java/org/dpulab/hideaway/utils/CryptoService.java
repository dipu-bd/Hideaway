/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        return sb.toString();
    }
    
    SecretKeySpec generateKeySpec(String password, String algorithm) throws UnsupportedEncodingException {
        byte[] key = password.getBytes("UTF-8");
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        SecretKeySpec spec = new SecretKeySpec(key, algorithm);
        return spec;
    }
    
    IvParameterSpec generateIV() {
        return new IvParameterSpec("1123581321345589".getBytes());
    }
    
    /**
     * Encrypt a plain text using AES encryption.
     * @param plainText The text to encrypt
     * @param password The password to use
     * @return The encrypted text
     * @throws GeneralSecurityException in case of failure in encryption.
     * @throws UnsupportedEncodingException 
     */
    public byte[] encryptAES(final byte[] plainText, String password) throws GeneralSecurityException, UnsupportedEncodingException {
        // Generate AES key spec
        password = this.getModifierPassword(password);
        SecretKeySpec key = generateKeySpec(password, "AES");
        
        // Decrypt using AES
        IvParameterSpec iv = this.generateIV();
        Cipher decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptor.init(Cipher.ENCRYPT_MODE, key, iv);
        return decryptor.doFinal(plainText);
    }
    
    /**
     * Encrypt a plain text using AES and store it into a file.
     * @param plainText The text to encrypt.
     * @param outputFile The output file.
     * @param password The password to use.
     * @throws java.security.GeneralSecurityException in case of failure in encryption.
     * @throws java.io.IOException in case of an I/O error.
     */
    public void saveEncrypted(final byte[] plainText, final File outputFile, String password) throws GeneralSecurityException, IOException {
        byte[] cipherText = this.encryptAES(plainText, password);
        FileUtils.writeByteArrayToFile(outputFile, cipherText);
    }
    
    /***
     * Decrypt a cipher text encrypted with AES.
     * @param cipherText The bytes to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException in case of failure in decryption.
     * @throws java.io.UnsupportedEncodingException in case UTF-8 encoding scheme not found.
     */
    public byte[] decryptAES(final byte[] cipherText, String password) throws GeneralSecurityException, UnsupportedEncodingException {
        // Generate AES key spec
        password = this.getModifierPassword(password);
        SecretKeySpec key = generateKeySpec(password, "AES");
        
        // Decrypt using AES
        IvParameterSpec iv = this.generateIV();
        Cipher decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptor.init(Cipher.DECRYPT_MODE, key, iv);
        return decryptor.doFinal(cipherText);
    }
    
    /**
     * Decrypt a file encrypted with AES and returns the plain text content.
     * @param inputFile The file to decrypt.
     * @param password The password to use.
     * @return The decrypted plain text.
     * @throws GeneralSecurityException in case of failure in decryption.
     * @throws IOException in case of an I/O error.
     */
    public byte[]  loadDecrypted(final File inputFile, String password) throws GeneralSecurityException, IOException {
        byte[] cipherText = FileUtils.readFileToByteArray(inputFile);
        return this.decryptAES(cipherText, password);
    }

}
