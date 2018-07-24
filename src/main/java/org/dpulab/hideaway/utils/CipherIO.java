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
    private IndexEntry rootEntry;

    private String password;
    private String passwordHash;

    private CipherIO(String folder)
            throws PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        this.workDir = new File(folder).toPath();
        this.rootEntry = IndexEntry.getRoot();
        this.loadPassword();
    }
    
    public final void loadPassword()
            throws PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        this.password = Settings.getDefault().getSession(Settings.PASSWORD);
        this.passwordHash = CryptoService.getDefault().getPasswordHash(this.password);

        if (StringUtils.isEmpty(this.password) || StringUtils.isEmpty(this.passwordHash)) {
            throw new PasswordException("No password was found");
        }
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

        // Create data folder if not exists
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            Reporter.put("Data folder is created.");
        }

        // Check the index file
        File indexFile = this.getIndexFile();
        if (!indexFile.exists()) {
            // verify the password
            this.confirmPassword();
            // create ans save an empty index
            indexFile.getParentFile().mkdirs();
            this.saveIndex();
        } else {
            this.loadIndex();
        }
    }

    public File getDataFolder() {
        return this.workDir.resolve("data").toFile();
    }

    public File getDataFile(String checksum) {
        return this.workDir.resolve("data").resolve(checksum).toFile();
    }

    public File getIndexFile() {
        return this.workDir.resolve(this.passwordHash + ".index").toFile();
    }

    public final IndexEntry getRootIndex() {
        return this.rootEntry;
    }
    
    /**
     * Verify current password by asking it again.
     *
     * @throws org.bouncycastle.openssl.PasswordException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public void confirmPassword()
        throws PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException {
        PasswordConfirm passwordInput = new PasswordConfirm(null);
        passwordInput.setVisible(true);
        passwordInput.dispose();
        // load new passwords
        this.loadPassword();
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
        Properties props = new Properties();
        Key key = CryptoService.getDefault().generateKey(this.password);
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
