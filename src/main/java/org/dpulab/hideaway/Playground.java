/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
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
        // generateKeysPair();
        commonsCryptoTest();
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

    void generateKeysPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
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

    void commonsCryptoTest() {
        System.out.println();
        System.out.println("Testing commons-crypto...");
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter message: ");
            String message = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.next();

            StringBuilder sb = new StringBuilder();
            while (sb.length() < 16) {
                sb.append(password);
                sb.reverse();
            }
            byte[] keyBytes = Arrays.copyOf(sb.toString().getBytes("UTF-8"), 16);
            System.out.printf("%s %d\n", new String(keyBytes), keyBytes.length);

            final String transformation = "AES/CBC/PKCS5Padding";
            Properties props = new Properties();
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec("ZGlwdQ49dm2kllfa".getBytes());
            
            byte[] cipherText;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (CryptoOutputStream cos = new CryptoOutputStream(transformation, props, baos, key, iv);
                    DataOutputStream dos = new DataOutputStream(cos)) {
                dos.writeUTF(message);
            }
            baos.close();
            cipherText = baos.toByteArray();
            System.out.println("Encrypted message:");
            System.out.println(Base64.getEncoder().encodeToString(cipherText));

            try(ByteArrayInputStream bais = new ByteArrayInputStream(cipherText);
                    CryptoInputStream cis = new CryptoInputStream(transformation, props, bais, key, iv);
                    DataInputStream dis = new DataInputStream(cis)) {
                System.out.println("Decrypted message:");
                System.out.println(dis.readUTF());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
