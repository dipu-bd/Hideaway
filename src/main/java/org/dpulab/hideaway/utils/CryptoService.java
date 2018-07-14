/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

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
    
    /**
     * Generate password key block of given size.
     * @param password The password to use (Should be minimum of 8 bytes).
     * @param blockSize The block size (Supported values: 16, 24, 32).
     * @return The password block of fixed size.
     * @throws UnsupportedEncodingException 
     */
    public static byte[] getKeyBlock(String password, int blockSize) throws UnsupportedEncodingException {        
        int sign = 1;
        while(password.length() < blockSize) {
            int rotc = sign * password.length() / 4;
            String temp = StringUtils.rotate(password, rotc);
            password = StringUtils.reverse(temp.concat(password));
            sign *= -1;
        }
        byte[] bytes = password.getBytes("UTF-8");
        return Arrays.copyOf(bytes, blockSize);
    }
    
}
