/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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
     * Get the hash of given text using SHA-1 algorithm.
     *
     * @param text The input text.
     * @return The hash of the text.
     */
    public String getHash(String text) {
        String hash = text;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
            text = StringUtils.swapCase(text);
            digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
            text = StringUtils.rotate(text, text.length() / 3);
            digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
            text = StringUtils.reverse(text);
            digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
            hash = Base64.getEncoder().encodeToString(digest.digest());
            hash = hash.replace('/', '-');
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(CryptoService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hash;
    }

    /**
     * Generate password key block of given size.
     *
     * @param password The password to use (Should be minimum of 8 bytes).
     * @param blockSize The block size (Supported values: 16, 24, 32).
     * @return The password block of fixed size.
     * @throws UnsupportedEncodingException
     */
    public static byte[] getKeyBlock(String password, int blockSize) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        while(sb.length() < blockSize) {
            sb.append(password);
            sb.reverse();
        }
        byte[] bytes = sb.toString().getBytes(Settings.DEFAULT_CHARSET);
        return Arrays.copyOf(bytes, blockSize);
    }

    /**
     * Converts integer to byte array.
     *
     * @param value
     * @return
     */
    public static byte[] toByteArray(int value) {
        return new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }

    /**
     * Converts byte array to integer.
     *
     * @param bytes
     * @return
     */
    public static int fromByteArray(byte[] bytes) {
        return (bytes[0] << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | ((bytes[3] & 0xFF));
    }

    /**
     * Generates a secret key using given password as a seed to a SecureRandom instance.
     * @param password The password to use as a seed.
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException 
     */
    public Key generateKey(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        int keyBitSize = 256;
        byte[] seed = password.getBytes(Settings.DEFAULT_CHARSET);
        SecureRandom secureRandom = new SecureRandom(seed);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        keyGen.init(keyBitSize, secureRandom);
        SecretKey secret = keyGen.generateKey();
                        
        return secret;
    }
    
    /**
     * Generates the algorithmic parameter specs for cipher.
     * @param seed The seed value.
     * @return
     * @throws UnsupportedEncodingException 
     */
    public AlgorithmParameterSpec generateParamSpec(String seed) throws UnsupportedEncodingException {
        byte[] block = CryptoService.getKeyBlock(seed, 16);
        return new IvParameterSpec(block);
    }

}
