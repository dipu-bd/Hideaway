/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

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
}
