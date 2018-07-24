/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author dipu
 */
public final class CryptoService {

    private static final CryptoService INSTANCE = new CryptoService();

    public static CryptoService getDefault() {
        return CryptoService.INSTANCE;
    }

    private CryptoService() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Converts integer to byte array.
     *
     * @param value
     * @return
     */
    public static byte[] toByteArray(int value) {
        return new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }

    /**
     * Converts byte array to integer.
     *
     * @param bytes
     * @return
     */
    public static int fromByteArray(byte[] bytes) {
        return (bytes[0] << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | ((bytes[3] & 0xFF));
    }

    public static String getBytePreview(byte[] data, int length) {
        if (data == null || data.length == 0 || length <= 0) {
            return "<< empty >>";
        }
        length = Math.min(length, data.length);
        String hex = Hex.toHexString(data, 0, length);
        hex = hex.replaceAll("..", "$0 ").toUpperCase().trim();
        if (length != data.length) {
            hex += " ...";
        }
        return hex;
    }

    public static String getKeyAsString(Key key) {
        String output = Base64.getEncoder().encodeToString(key.getEncoded());
        if ("X.509".equals(key.getFormat())) {
            output = WordUtils.wrap(output, 64, "\n", true);
            output = "-----BEGIN PUBLIC KEY-----\n" + output + "\n-----END PUBLIC KEY-----";
        } else if ("PKCS#8".equals(key.getFormat())) {
            output = WordUtils.wrap(output, 64, "\n", true);
            output = "-----BEGIN RSA PRIVATE KEY-----\n" + output + "\n-----END RSA PRIVATE KEY-----";
        }
        return output;
    }

    /**
     * Generate password key block of given size.
     *
     * @param password The password to use (Should be minimum of 8 bytes).
     * @param blockSize The block size (Supported values: 16, 24, 32).
     * @return The password block of fixed size.
     * @throws UnsupportedEncodingException
     */
    public static byte[] getPasswordBlock(String password, int blockSize) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < blockSize) {
            sb.append(password);
            sb.reverse();
        }
        byte[] bytes = sb.toString().getBytes(Settings.DEFAULT_CHARSET);
        return Arrays.copyOf(bytes, blockSize);
    }

    /**
     * Get the hash of given text using SHA-1 algorithm.
     *
     * @param text The input text.
     * @return The hash of the text.
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public String getHash(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
        text = StringUtils.swapCase(text);
        digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
        text = StringUtils.rotate(text, text.length() / 3);
        digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
        text = StringUtils.reverse(text);
        digest.update(text.getBytes(Settings.DEFAULT_CHARSET));
        String hash = Base64.getEncoder().encodeToString(digest.digest());
        hash = hash.replace('/', '-');
        return hash;
    }

    /**
     * Gets the checksum of the byte array. It calculates the hash of the array
     * using SHA-512 algorithm.
     *
     * @param fullPath
     * @param data file path
     * @return the checksum string
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public String getChecksum(String fullPath, byte[] data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (StringUtils.isEmpty(fullPath) || data == null) {
            return null;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(data);
        digest.update(fullPath.getBytes(Settings.DEFAULT_CHARSET));
        String hash = Base64.getEncoder().encodeToString(digest.digest());
        hash = hash.replace('/', '-');
        return hash;
    }

    /**
     * Generates a secret key using given password as a seed to a SecureRandom
     * instance.
     *
     * @param password The password to use as a seed.
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public Key generateKey(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        int keyBitSize = 256;
        byte[] seed = password.getBytes(Settings.DEFAULT_CHARSET);
        SecureRandom secureRandom = new SecureRandom(seed);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        keyGen.init(keyBitSize, secureRandom);
        SecretKey secret = keyGen.generateKey();

        return secret;
    }

    /**
     * Generates the algorithmic parameter specs for cipher.
     *
     * @param seed The seed value.
     * @return
     * @throws UnsupportedEncodingException
     */
    public AlgorithmParameterSpec generateParamSpec(String seed) throws UnsupportedEncodingException {
        byte[] block = CryptoService.getPasswordBlock(seed, 16);
        return new IvParameterSpec(block);
    }

    /**
     * Generates a bit RSA KeyPair using BountyCastle provider.
     *
     * @param bitSize the bits in key. To be safe, use either 2048 or 4096.
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.NoSuchProviderException
     */
    public KeyPair generateKeyPair(int bitSize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(bitSize, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * Generates an instance of X500Name.
     *
     * @param alias
     * @param clientName
     * @param clientEmail
     * @param organizationalUnit
     * @param organization
     * @param countryCode
     * @return
     */
    public X500Name generateX500Name(
            String alias,
            String clientName,
            String clientEmail,
            String organizationalUnit,
            String organization,
            String countryCode
    ) {
        if (StringUtils.isEmpty(alias)) {
            return null;
        }
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, alias);
        if (!StringUtils.isEmpty(clientName)) {
            builder.addRDN(BCStyle.NAME, clientName);
        }
        if (!StringUtils.isEmpty(clientEmail)) {
            builder.addRDN(BCStyle.E, clientEmail);
        }
        if (!StringUtils.isEmpty(countryCode)) {
            builder.addRDN(BCStyle.C, countryCode);
        }
        if (!StringUtils.isEmpty(organization)) {
            builder.addRDN(BCStyle.O, organization);
        }
        if (!StringUtils.isEmpty(organizationalUnit)) {
            builder.addRDN(BCStyle.OU, organizationalUnit);
        }
        return builder.build();
    }

    /**
     * Generates a self signed certificate for the keyPair with provided
     * subject.
     *
     * @param keyPair the RSA key pair
     * @param subject the subject's information
     * @return a certificate
     * @throws org.bouncycastle.cert.CertIOException
     * @throws org.bouncycastle.operator.OperatorCreationException
     * @throws java.security.cert.CertificateException
     */
    public X509Certificate generateSelfSignedX509Certificate(KeyPair keyPair, X500Name subject) throws IOException, OperatorCreationException, CertificateException {
        X500Name issuer = generateX500Name("dpulab", "Hideaway", "dipu.sudipta@gmail.com", "org", "dpulab", "BD");
        // use 100 year validity between start and end dates
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 100); // 100 Yr validity
        Date endDate = calendar.getTime();
        // generate a serial number
        SecureRandom random = new SecureRandom();
        BigInteger serial = new BigInteger(160, random);
        // Gets an instance of builder
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                startDate,
                endDate,
                subject,
                keyPair.getPublic());
        // set the key identifiers extension
        byte[] id = new byte[24];
        random.nextBytes(id);
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, id);
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, id);
        // creates basic constraints
        BasicConstraints constraints = new BasicConstraints(true);
        certBuilder.addExtension(Extension.basicConstraints, true, constraints.getEncoded());
        // add the key usage extension
        KeyUsage usage = new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.digitalSignature);
        certBuilder.addExtension(Extension.keyUsage, false, usage.getEncoded());
        // extend key usage and assign purposes
        /* Vector usages = new Vector();
        usages.add(KeyPurposeId.id_kp_serverAuth);
        usages.add(KeyPurposeId.id_kp_clientAuth);
        ExtendedKeyUsage usageEx = new ExtendedKeyUsage(usages);
        certBuilder.addExtension(Extension.extendedKeyUsage, false, usageEx.getEncoded()); */

        // Build BouncyCastle certificate
        String signatureAlgorithm = "SHA256WithRSA";
        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());
        X509CertificateHolder holder = certBuilder.build(contentSigner);

        // Converts to JRE certificate
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(new BouncyCastleProvider());
        X509Certificate x509 = converter.getCertificate(holder);

        return x509;
    }

}
