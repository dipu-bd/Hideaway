/*
 * Copyright (C) 2018 dipu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dpulab.hideaway.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bouncycastle.openssl.PasswordException;
import org.dpulab.hideaway.models.IndexEntry;
import org.dpulab.hideaway.view.PasswordInput;

/**
 *
 * @author dipu
 */
public class CipherIO {

    public static final String SEPARATOR = "/";

    //<editor-fold defaultstate="collapsed" desc=" Get instance methods ">
    private static final HashMap<String, CipherIO> STORAGE = new HashMap<>();

    public static CipherIO getFor(String folder)
            throws KeyStoreException, PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (!CipherIO.STORAGE.containsKey(folder)) {
            // attach a new storage class with the folder
            CipherIO.STORAGE.put(folder, new CipherIO(folder));
        }
        return CipherIO.STORAGE.get(folder);
    }

    public static CipherIO getDefault()
            throws KeyStoreException, PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String folder = Settings.getDefault().get(Settings.WORK_DIR);
        return CipherIO.getFor(folder);
    }
    //</editor-fold>

    private final Path workDir;
    private final KeyStore keyStore;
    private IndexEntry rootEntry;

    private final String password;
    private final String passwordHash;

    private CipherIO(String folder)
            throws KeyStoreException, PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        this.workDir = new File(folder).toPath();
        this.keyStore = KeyStore.getInstance(Settings.STORE_TYPE);
        this.rootEntry = IndexEntry.getRoot();

        this.password = Settings.getDefault().getSession(Settings.PASSWORD);
        this.passwordHash = CryptoService.getDefault().getHash(this.password);

        if (StringUtils.isEmpty(this.password) || StringUtils.isEmpty(this.passwordHash)) {
            throw new PasswordException("No password was found");
        }
    }

    public File getDataFolder() {
        return this.workDir.resolve("data").toFile();
    }

    public File getDataFile(String checksum) {
        return this.workDir.resolve("data").resolve(checksum).toFile();
    }

    public File getKeyStoreFile() {
        return this.workDir.resolve(this.passwordHash + ".jks").toFile();
    }

    public File getIndexFile() {
        return this.workDir.resolve(this.passwordHash + ".index").toFile();
    }

    public char[] getKeystorePass() {
        return this.password.toCharArray();
    }

    /**
     * Checks the folder
     *
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws ClassNotFoundException
     */
    public void checkFolder() throws IOException, GeneralSecurityException, ClassNotFoundException {
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
            }
            // create ans save an empty index
            indexFile.getParentFile().mkdirs();
            this.saveIndex();
        } else {
            this.loadIndex();
        }
    }

    /**
     * Get the key to encrypt/decrypt the index file.
     *
     * @return The saved key or a new key.
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     * @throws java.security.UnrecoverableKeyException
     * @throws UnsupportedEncodingException
     */
    public Key getIndexSecret()
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        if (!this.keyStore.containsAlias(Settings.INDEX_KEY_ALIAS)) {
            Key key = CryptoService.getDefault().generateKey(this.password);
            this.storeSecretKey(Settings.INDEX_KEY_ALIAS, key);
        }
        Key key = this.keyStore.getKey(Settings.INDEX_KEY_ALIAS, this.getKeystorePass());
        return key;
    }

    /**
     * Stores a secret symmetric key.
     *
     * @param alias
     * @param secret
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public void storeSecretKey(String alias, Key secret)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        this.keyStore.setKeyEntry(alias, secret, this.getKeystorePass(), null);
        this.saveKeystore();
    }

    /**
     * Gets a RSA key pair key by its alias.
     *
     * @param alias
     * @return
     * @throws java.security.KeyStoreException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.UnrecoverableKeyException
     */
    public KeyPair getKeyPair(String alias)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        PublicKey publicKey = this.keyStore.getCertificate(alias + "_cert").getPublicKey();
        PrivateKey privateKey = (PrivateKey) this.keyStore.getKey(alias + "_key", this.getKeystorePass());
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Sets a secret key by its alias. Certificate chain can be null unless it
     * is a RSA Key pair.
     *
     * @param alias
     * @param keyPair
     * @param certificate
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public void storeKeyPair(String alias, KeyPair keyPair, Certificate certificate)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Certificate[] certChain = {certificate};
        this.keyStore.setCertificateEntry(alias + "_cert", certificate);
        this.keyStore.setKeyEntry(alias + "_key", keyPair.getPrivate(), this.getKeystorePass(), certChain);
        this.saveKeystore();
    }

    /**
     * Checks if a RSA KeyPair exists in current KeyStore by its alias.
     *
     * @param alias the alias of the key pair
     * @return
     * @throws KeyStoreException
     */
    public boolean containsKeyPair(String alias) throws KeyStoreException {
        return this.keyStore.containsAlias(alias + "_cert")
                && this.keyStore.containsAlias(alias + "_key");
    }

    /**
     * Gets a collection of default system properties.
     *
     * @return
     */
    public Properties defaultProperties() {
        return new Properties();
        // org.apache.commons.crypto.utils.Utils.getDefaultProperties();
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

        for (String alias : Collections.list(this.keyStore.aliases())) {
            if (this.keyStore.isKeyEntry(alias)) {
                Key key = this.keyStore.getKey(alias, this.getKeystorePass());
                System.out.printf("%s %s %s\n", alias, key.getAlgorithm(), key.getFormat());
                System.out.println(WordUtils.wrap(Base64.getEncoder().encodeToString(key.getEncoded()), 64, "\n", true));
            } else if (this.keyStore.isCertificateEntry(alias)) {
                Certificate cert = this.keyStore.getCertificate(alias);
                System.out.printf("%s %s\n", alias, cert.getType());
                System.out.println(WordUtils.wrap(Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded()), 64, "\n", true));
            }
        }
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
    public void saveKeystore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        File keyStoreFile = this.getKeyStoreFile();
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
            this.keyStore.store(fos, this.getKeystorePass());
        }
        Reporter.format("Keystore saved with %s keys.", this.keyStore.size());
    }

    /**
     * Loads the root index entry from the index file
     *
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws java.lang.UnsupportedClassVersionError
     */
    public void loadIndex() throws IOException, GeneralSecurityException, UnsupportedClassVersionError {
        File indexFile = this.getIndexFile();
        Key key = this.getIndexSecret();
        Properties props = this.defaultProperties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileInputStream fis = new FileInputStream(indexFile);
                CryptoInputStream cis = new CryptoInputStream(Settings.AES_ALGORITHM, props, fis, key, params);
                ObjectInputStream ois = new ObjectInputStream(cis)) {
            rootEntry = IndexEntry.readExternal(ois);
        }
        Reporter.format("Index entry loaded. Total file size: %s", rootEntry.getFileSizeReadable());
    }

    /**
     * Saves the root index entry to the index file.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void saveIndex() throws IOException, GeneralSecurityException {
        File indexFile = this.getIndexFile();
        Key key = this.getIndexSecret();
        Properties props = this.defaultProperties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileOutputStream fos = new FileOutputStream(indexFile);
                CryptoOutputStream cos = new CryptoOutputStream(Settings.AES_ALGORITHM, props, fos, key, params);
                ObjectOutputStream oos = new ObjectOutputStream(cos)) {
            rootEntry.writeExternal(oos);
        } catch (Exception ex) {
            indexFile.delete();
            throw ex;
        }
        Reporter.format("Index entry saved. Total file size: %s", rootEntry.getFileSizeReadable());
    }
}
