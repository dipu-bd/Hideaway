/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.dpulab.hideaway.models.CipherFile;
import org.dpulab.hideaway.view.PasswordInput;

/**
 *
 * @author dipu
 */
public class CipherIO {

    static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    static final String INDEX_KEY_ALIAS = "INDEX_PASSWD";
    static final String INDEX_IV_ALIAS = "INDEX_IvParam";

    //<editor-fold defaultstate="collapsed" desc=" Get instance methods ">
    private static final HashMap<String, CipherIO> STORAGE = new HashMap<>();

    public static CipherIO getFor(String folder) throws KeyStoreException {
        if (!CipherIO.STORAGE.containsKey(folder)) {
            // attach a new storage class with the folder
            CipherIO.STORAGE.put(folder, new CipherIO(folder));
        }
        return CipherIO.STORAGE.get(folder);
    }

    public static CipherIO getDefault() throws KeyStoreException {
        String folder = Settings.getDefault().get("WORK_DIRECTORY");
        return CipherIO.getFor(folder);
    }
    //</editor-fold>

    private final Path workDir;
    private final KeyStore keyStore;
    private final ArrayList<CipherFile> fileList;

    private final String password;
    private final String passwordHash;

    private CipherIO(String folder) throws KeyStoreException {
        this.workDir = new File(folder).toPath();
        this.keyStore = KeyStore.getInstance("PKCS12");
        this.fileList = new ArrayList<>();

        this.password = Settings.getDefault().getSession("PASSWORD");
        this.passwordHash = CryptoService.getDefault().getHash(this.password);
    }

    public File getDataFolder() {
        return this.workDir.resolve("data").toFile();
    }

    public File getKeyStoreFile() {
        return this.workDir.resolve(this.passwordHash + ".ks").toFile();
    }

    public File getIndexFile() {
        return this.workDir.resolve(this.passwordHash + ".index").toFile();
    }

    public char[] getKeystorePass() {
        return this.password.toCharArray();
    }

    public void checkFolder() throws IOException, GeneralSecurityException {
        // Check the working directory
        File folder = this.workDir.toFile();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new FileSystemException("Failed to create the work directory");
            }
            Reporter.put("Created work directory");
        } else if (!folder.canWrite()) {
            throw new FileSystemException("The work directory do not have write permission");
        } else if (!folder.isDirectory()) {
            throw new FileSystemException("The work directory is not a folder");
        }

        boolean passwordVerified = false;

        // Create data folder if not exists
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            Reporter.put("Data folder is created.");
        }

        // Load the keystore
        File keyStoreFile = this.getKeyStoreFile();
        if (!keyStoreFile.exists()) {
            // verify the password
            this.confirmPassword();
            passwordVerified = true;
            // create and save an empty keystore
            this.keyStore.load(null, this.getKeystorePass());
            keyStoreFile.getParentFile().mkdirs();
            this.saveKeystore();
        } else {
            this.loadKeystore();
        }

        // Check the index file
        File indexFile = this.getIndexFile();
        if (!indexFile.exists()) {
            // verify the password
            if (!passwordVerified) {
                this.confirmPassword();
                passwordVerified = true;
            }
            // create ans save an empty index
            indexFile.getParentFile().mkdirs();
            this.fileList.clear();
            this.saveIndex();
        } else {
            this.loadIndex();
        }

        // Verify the index entry
        this.verifyFileList();
    }

    /**
     * Get the key to encrypt/decrypt the index file.
     *
     * @return The saved key or a new key.
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws UnrecoverableKeyException
     */
    public Key getIndexSecret() throws IOException, GeneralSecurityException {
        if (!this.keyStore.containsAlias(INDEX_KEY_ALIAS)) {
            Key key = CryptoService.getDefault().generateKey(this.password);
            this.keyStore.setKeyEntry(INDEX_KEY_ALIAS, key, this.getKeystorePass(), null);
        }
        Key key = this.keyStore.getKey(INDEX_KEY_ALIAS, this.getKeystorePass());
        return key;
    }

    /**
     * Gets a collection of default system properties.
     *
     * @return
     */
    public Properties defaultProperties() {
        return org.apache.commons.crypto.utils.Utils.getDefaultProperties();
    }

    /**
     * Verify current password by asking it again.
     *
     * @throws java.nio.file.AccessDeniedException
     */
    public void confirmPassword() throws AccessDeniedException {
        PasswordInput passwordInput = new PasswordInput(null);
        passwordInput.setVisible(true);
        String verifyPassword = passwordInput.getPassword();
        passwordInput.dispose();
        if (!this.password.equals(verifyPassword)) {
            throw new AccessDeniedException("Retyped password did not match the initial one");
        }
    }

    /**
     * Loads the current key-store from file.
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws java.security.KeyStoreException
     */
    public void loadKeystore() throws IOException, GeneralSecurityException {
        File keyStoreFile = this.getKeyStoreFile();
        try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
            this.keyStore.load(fis, this.getKeystorePass());
        }
        Reporter.format("Keystore loaded with %d keys.", this.keyStore.size());
    }

    /**
     * Store the current key-store to file.
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws java.security.KeyStoreException
     */
    public void saveKeystore() throws IOException, GeneralSecurityException {
        File keyStoreFile = this.getKeyStoreFile();
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
            this.keyStore.store(fos, this.getKeystorePass());
        }
        Reporter.format("Keystore saved with %s keys.", this.keyStore.size());
    }

    public void loadIndex() throws IOException, GeneralSecurityException {
        File indexFile = this.getIndexFile();
        Key key = this.getIndexSecret();
        Properties props = this.defaultProperties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileInputStream fis = new FileInputStream(indexFile);
                CryptoInputStream cos = new CryptoInputStream(AES_ALGORITHM, props, fis, key, params)) {
            byte[] total = new byte[4];
            cos.read(total);
            this.fileList.clear();
            for (int i = 0; i < CryptoService.fromByteArray(total); ++i) {
                CipherFile file = CipherFile.fromStream(cos);
                this.fileList.add(file);
            }
        }
        Reporter.format("Index entry loaded. %d files", this.fileList.size());
    }

    public void saveIndex() throws IOException, GeneralSecurityException {
        File indexFile = this.getIndexFile();
        Key key = this.getIndexSecret();
        Properties props = this.defaultProperties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileOutputStream fos = new FileOutputStream(indexFile);
                CryptoOutputStream cos = new CryptoOutputStream(AES_ALGORITHM, new Properties(), fos, key, params)) {
            cos.write(CryptoService.toByteArray(this.fileList.size()));
            for (CipherFile cipherFile : this.fileList) {
                cipherFile.writeBytes(cos);
            }
        }
        Reporter.format("Index entry saved. Total files: %s", this.fileList.size());
    }

    // TODO: Verify file list
    public void verifyFileList() {
    }

    public void addFile(File sourceFile, String destFolder) {

    }
}
