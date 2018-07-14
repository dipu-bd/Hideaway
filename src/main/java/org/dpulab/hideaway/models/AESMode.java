/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.dpulab.hideaway.utils.CryptoService;

/**
 *
 * @author dipu
 */
public class AESMode {

    private final int blockSize = 32;
    private final SecretKeySpec secretKey;
    private final AlgorithmParameterSpec paramSpec;
    private final String transformer = "AES/GCM/PKCS5Padding";
    private final Properties properties;

    public AESMode(String password) throws UnsupportedEncodingException {
        this.properties = new Properties();

        byte[] keyBlock = CryptoService.getKeyBlock(password, this.blockSize);
        this.secretKey = new SecretKeySpec(keyBlock, "AES");

        byte[] ivBlock = CryptoService.getKeyBlock("12e2e2f358a", this.blockSize);
        this.paramSpec = new GCMParameterSpec(this.blockSize * 8, ivBlock);
    }

    public CryptoInputStream getInputStream(InputStream inputStream) throws IOException {
        return new CryptoInputStream(
                this.transformer,
                this.properties,
                inputStream,
                this.secretKey,
                this.paramSpec);
    }

    public CryptoOutputStream getOutputStream(OutputStream outputStream) throws IOException {
        return new CryptoOutputStream(
                this.transformer,
                this.properties,
                outputStream,
                this.secretKey,
                this.paramSpec);
    }

    public Cipher getCipher(int mode) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(this.transformer);
        cipher.init(mode, this.secretKey, this.paramSpec);
        return cipher;
    }

    public byte[] encrypt(byte[] data) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        return getCipher(Cipher.ENCRYPT_MODE).doFinal(data);
    }

    public byte[] decrypt(byte[] data) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        return getCipher(Cipher.DECRYPT_MODE).doFinal(data);
    }
}
