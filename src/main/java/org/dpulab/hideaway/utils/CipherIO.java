/*
 * Copyright (C) 2018 Sudipto Chandra
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import javax.crypto.Cipher;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.PasswordException;
import org.dpulab.hideaway.models.IndexEntry;
import org.dpulab.hideaway.view.PasswordConfirm;

/**
 *
 * @author dipu
 */
public class CipherIO {

    public static final String SEPARATOR = "/";

    //<editor-fold defaultstate="collapsed" desc=" Get instance methods ">
    private static final HashMap<String, CipherIO> STORAGE = new HashMap<>();

    public static CipherIO intanceFor(String folder)
            throws KeyStoreException, PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (!CipherIO.STORAGE.containsKey(folder)) {
            // attach a new storage class with the folder
            CipherIO.STORAGE.put(folder, new CipherIO(folder));
        }
        return CipherIO.STORAGE.get(folder);
    }

    public static CipherIO instance()
            throws KeyStoreException, PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String folder = Settings.getDefault().get(Settings.WORK_DIR);
        return CipherIO.intanceFor(folder);
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

    public final KeyStore getKeyStore() {
        return this.keyStore;
    }

    public final IndexEntry getRootIndex() {
        return this.rootEntry;
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
     * Gets a secret entry by its alias.
     *
     * @param alias a string that ends with <code>_key</code>
     * @return a private key
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public Key getSecretKey(String alias) throws IOException, GeneralSecurityException {
        if (!this.keyStore.containsAlias(alias)) {
            Key key = CryptoService.getDefault().generateKey(alias);
            this.storeSecretKey(alias, key);
        }
        Key key = this.keyStore.getKey(alias, this.getKeystorePass());
        return key;
    }

    /**
     * Get the key to encrypt/decrypt the index file.
     *
     * @return The saved key or a new key.
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public Key getIndexSecret() throws IOException, GeneralSecurityException {
        return this.getSecretKey(Settings.INDEX_KEY_ALIAS);
    }

    /**
     * Delete a key entry by its alias.
     *
     * @param alias
     * @throws KeyStoreException
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     */
    public void deleteKeyEntry(String alias)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        // delete this alias
        if (this.keyStore.containsAlias(alias)) {
            this.keyStore.deleteEntry(alias);
        }
        // save keystore
        this.saveKeystore();
    }

    /**
     * Gets the key by an alias. The key can be of any type: AES, RSA, or
     * certificate.
     *
     * @param alias the alias of the key store.
     * @return the key
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public Key getKeyEntry(String alias)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (this.keyStore.isCertificateEntry(alias)) {
            return this.keyStore.getCertificate(alias).getPublicKey();
        }
        return this.keyStore.getKey(alias, this.getKeystorePass());
    }

    /**
     * Gets a public key entry by its alias.
     *
     * @param alias a string that ends with <code>_cert</code>
     * @return a public key
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public PublicKey getPublicKey(String alias)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PublicKey) this.getKeyEntry(alias + "_cert");
    }

    /**
     * Gets a private key entry by its alias.
     *
     * @param alias a string that ends with <code>_key</code>
     * @return a public key
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public PrivateKey getPrivateKey(String alias)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) this.getKeyEntry(alias + "_key");
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
        return new KeyPair(this.getPublicKey(alias), this.getPrivateKey(alias));
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
     * Gets a list of all available key-pair alias.
     *
     * @return
     * @throws java.security.KeyStoreException
     */
    public String[] allKeyPairAliases() throws KeyStoreException {
        ArrayList<String> result = new ArrayList<>();
        Enumeration<String> aliases = this.keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (this.keyStore.isCertificateEntry(alias)) {
                alias = StringUtils.removeEnd(alias, "_cert");
                if (this.containsKeyPair(alias)) {
                    result.add(alias);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Verify current password by asking it again.
     *
     * @throws java.nio.file.AccessDeniedException
     */
    public void confirmPassword() throws AccessDeniedException {
        PasswordConfirm passwordInput = new PasswordConfirm(null);
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
        Properties props = new Properties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileInputStream fis = new FileInputStream(indexFile);
                CryptoInputStream cis = new CryptoInputStream(Settings.AES_CBC_PKCS5, props, fis, key, params);
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
        Properties props = new Properties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileOutputStream fos = new FileOutputStream(indexFile);
                CryptoOutputStream cos = new CryptoOutputStream(Settings.AES_CBC_PKCS5, props, fos, key, params);
                ObjectOutputStream oos = new ObjectOutputStream(cos)) {
            rootEntry.writeExternal(oos);
        } catch (Exception ex) {
            indexFile.delete();
            throw ex;
        }
        Reporter.format("Index entry saved. Total file size: %s", rootEntry.getFileSizeReadable());
    }

    /**
     * Read bytes from cipher file.
     *
     * @param entry
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws UnsupportedClassVersionError
     */
    public byte[] readFromCipherFile(IndexEntry entry) throws IOException, GeneralSecurityException, UnsupportedClassVersionError {
        File file = this.getDataFile(entry.getChecksum());
        if (!file.exists()) {
            return null;
        }

        PrivateKey key = this.getPrivateKey(entry.getKeyAlias());
        
        File indexFile = this.getIndexFile();
        Key key = this.getIndexSecret();
        Properties props = new Properties();
        AlgorithmParameterSpec params = CryptoService.getDefault().generateParamSpec(this.password);

        try (FileInputStream fis = new FileInputStream(indexFile);
                CryptoInputStream cis = new CryptoInputStream(Settings.AES_CBC_PKCS5, props, fis, key, params);
                ObjectInputStream ois = new ObjectInputStream(cis)) {
            rootEntry = IndexEntry.readExternal(ois);
        }

        try (FileOutputStream fos = new FileOutputStream(indexFile);
                CryptoOutputStream cos = new CryptoOutputStream(Settings.AES_CBC_PKCS5, props, fos, key, params);
                ObjectOutputStream oos = new ObjectOutputStream(cos)) {
            rootEntry.writeExternal(oos);
        } catch (Exception ex) {
            indexFile.delete();
            throw ex;
        }
        Cipher cipher = Cipher.getInstance(Settings.RSA_ALGORITHM, "BC");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] buffer = FileUtils.readFileToByteArray(file);
        byte[] decrypted = cipher.doFinal(buffer);
        return decrypted;
    }

    /**
     * Writes all bytes to cipher file.
     *
     * @param entry index entry of the file.
     * @param buffer the data to write.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void writeToCipherFile(IndexEntry entry, byte[] buffer) throws IOException, GeneralSecurityException {
        File file = this.getDataFile(entry.getChecksum());
        file.createNewFile();

        PrivateKey key = this.getPrivateKey(entry.getKeyAlias());

        Cipher cipher = Cipher.getInstance(Settings.RSA_ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(buffer);
        FileUtils.writeByteArrayToFile(file, encrypted);
    }

}
