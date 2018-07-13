/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author dipu
 */
public class Playground {
    
    public Playground() {
        // Security.addProvider(new BouncyCastlePQCProvider());
    }

    void play() {
        System.out.println("Start playing with various things...");
        AESEncryption();
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
            
            ByteOutputStream bos = new ByteOutputStream(16);
            bos.write(password.getBytes("UTF-8"));
            byte[] keyBytes = bos.getBytes();
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
}
