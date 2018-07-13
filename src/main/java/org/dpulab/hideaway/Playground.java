/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway;

import java.security.Security;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

/**
 *
 * @author dipu
 */
public class Playground {
    
    public Playground() {
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    void play() {
        System.out.println("Start playing with various things...");
        
        Scanner scanner = new Scanner(System.in);
        try {            
            System.out.print("Enter message: ");
            String message = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.next();
            
            byte[] keyBytes = password.getBytes("UTF-8");
            SecretKeySpec key = new SecretKeySpec(keyBytes, "RawBytes");
            
            Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] plainText = message.getBytes("UTF-8");
            byte[] cipherText = encrypt.doFinal(plainText);
            System.out.println("Encrypted message:");
            System.out.println(cipherText.toString());
            
            Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decipherText = decrypt.doFinal(cipherText);
            System.out.println("Decrypted message:");
            System.out.println(decipherText.toString());
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
