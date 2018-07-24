/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.text.WordUtils;

/**
 *
 * @author dipu
 */
public class Playground {

    public Playground() {
//         Security.addProvider(new BouncyCastlePQCProvider());
    }

    void play() {
        System.out.println("Start playing with various things...");
        // AESEncryption();
        // generateKeysPair("dipu");
        // commonsCryptoTest();
        generateKey("dipu");
        System.exit(0);
    }

    void AESEncryption() {
        System.out.println();
        System.out.println("Testing out AES encryption...");
        Scanner scanner = new Scanner(System.in);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("ZGlwdQ49dm2kllfa".getBytes());

            System.out.print("Enter message: ");
            String message = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.next();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
            baos.write(password.getBytes("UTF-8"));
            byte[] keyBytes = baos.toByteArray();
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            System.out.println("Encryption Key: ");
            System.out.println(Base64.getEncoder().encodeToString(keyBytes));

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] plainText = message.getBytes("UTF-8");
            byte[] cipherText = cipher.doFinal(plainText);
            System.out.println("Encrypted message:");
            System.out.println(Base64.getEncoder().encodeToString(cipherText));

            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] decipherText = cipher.doFinal(cipherText);
            System.out.println("Decrypted message:");
            System.out.println(new String(decipherText, "UTF-8"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void generateKeysPair(String password) {
        try {
            byte[] seed = password.getBytes("UTF-8");
            SecureRandom secureRandom = new SecureRandom(seed);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            byte[] pub = keyPair.getPublic().getEncoded();
            String out = Base64.getEncoder().encodeToString(pub);
            
            System.out.println("-----BEGIN RSA PUBLIC KEY-----");
            System.out.println(WordUtils.wrap(out, 64, "\n", true));
            System.out.println("-----END RSA PUBLIC KEY-----");
            System.out.println();

            byte[] pvt = keyPair.getPrivate().getEncoded();
            out = Base64.getEncoder().encodeToString(pvt);
            System.out.println("-----BEGIN RSA PRIVATE KEY-----");
            System.out.println(WordUtils.wrap(out, 64, "\n", true));
            System.out.println("-----END RSA PRIVATE KEY-----");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    public void generateKey(String password) {
        try {
            int keyBitSize = 256;
            byte[] seed = password.getBytes("UTF-8");
            SecureRandom secureRandom = new SecureRandom(seed);
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                        
            keyGen.init(keyBitSize, secureRandom);
            SecretKey secret = keyGen.generateKey();
                        
            System.out.println("Secret Key: " + secret.getFormat() + ", " + secret.getAlgorithm());
            System.out.println(Base64.getEncoder().encodeToString(secret.getEncoded()));
            
            Properties props = new Properties();
            for (String key : props.stringPropertyNames()) {
                System.out.println(key + " - " + props.getProperty(key));
            }
        } catch (Exception ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
