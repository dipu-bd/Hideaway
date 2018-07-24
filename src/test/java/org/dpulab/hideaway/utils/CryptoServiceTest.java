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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author dipu
 */
public class CryptoServiceTest {

    public CryptoServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println(StringUtils.center(" CryptoService ", 80, "="));
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void showHeader(String text) {
        System.out.println(StringUtils.center(" " + text + " ", 60, '-'));
    }

    /**
     * Test of getDefault method, of class CryptoService.
     */
    @org.junit.Test
    public void testGetDefault() {
        showHeader("getDefault");
        CryptoService instance = CryptoService.getDefault();
        assertNotNull(instance);
    }

    /**
     * Test of getHash method, of class CryptoService.
     *
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    @org.junit.Test
    public void testGetHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        showHeader("getHash");
        CryptoService instance = CryptoService.getDefault();
        String hash = instance.getHash("dipu");
        assertNotNull(hash);
        assertNotEquals("dipu", hash);
        assertFalse(StringUtils.containsAny(hash, '/', ':', '|', '\\', '$', '%', '!', '?', '{', '}'));

        hash = instance.getHash("");
        assertNull(hash);

        hash = instance.getHash(null);
        assertNull(hash);
    }

    /**
     * Test of getBytePreview method, of class CryptoService.
     *
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @org.junit.Test
    public void testGetBytePreview() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        showHeader("getBytePreview");
        String output = CryptoService.getBytePreview(null, 0);
        assertEquals("<< empty >>", output);

        output = CryptoService.getBytePreview("".getBytes(), 100);
        assertEquals("<< empty >>", output);

        output = CryptoService.getBytePreview("test".getBytes(), 0);
        assertEquals("<< empty >>", output);

        output = CryptoService.getBytePreview("hi".getBytes(), -199);
        assertEquals("<< empty >>", output);

        output = CryptoService.getBytePreview("".getBytes(), 100);
        assertEquals("<< empty >>", output);

        output = CryptoService.getBytePreview("dipu".getBytes(), 100);
        assertEquals("64 69 70 75", output);

        output = CryptoService.getBytePreview("dipu".getBytes(), 4);
        assertEquals("64 69 70 75", output);

        output = CryptoService.getBytePreview("dipu".getBytes(), 3);
        assertEquals("64 69 70 ...", output);

        output = CryptoService.getBytePreview("my name is dipu".getBytes(), 8);
        assertEquals("6D 79 20 6E 61 6D 65 20 ...", output);
    }

    /**
     * Test of getChecksum method, of class CryptoService.
     *
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    @org.junit.Test
    public void testGetChecksum() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        showHeader("getChecksum");
        CryptoService instance = CryptoService.getDefault();
        String hash = instance.getChecksum("dipu", "dipu".getBytes("utf-8"));
        assertNotNull(hash);
        assertNotEquals("dipu", hash);
        assertFalse(StringUtils.containsAny(hash, '/', ':', '|', '\\', '$', '%', '!', '?', '{', '}'));

        hash = instance.getChecksum("/usr/bin", "".getBytes("utf-8"));
        assertNotNull(hash);
        assertFalse(StringUtils.containsAny(hash, '/', ':', '|', '\\', '$', '%', '!', '?', '{', '}'));
        
        hash = instance.getChecksum("/usr/bin", "".getBytes("utf-8"));
        assertNotNull(hash);
        assertFalse(StringUtils.containsAny(hash, '/', ':', '|', '\\', '$', '%', '!', '?', '{', '}'));
        
        hash = instance.getChecksum("", null);
        assertNull(hash);

        hash = instance.getChecksum("", "".getBytes("utf-8"));
        assertNull(hash);
    }

    /**
     * Test of getPasswordBlock method, of class CryptoService.
     */
    @org.junit.Test
    public void testGetKeyBlock() throws Exception {
        showHeader("getKeyBlock");
        int blockSize = 16;
        String[] passwords = {
            "dipu",
            "sudipto",
            "AReallyLongText",
            "a very long Key with Spaces"
        };
        for (String password : passwords) {
            byte[] result = CryptoService.getPasswordBlock(password, blockSize);
            System.out.println(">>> " + password + " = " + new String(result));
        }
    }

    /**
     * Test of toByteArray method, of class CryptoService.
     */
    @org.junit.Test
    public void testToByteArray() {
        showHeader("toByteArray");
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            int value = random.nextInt();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(value);
            byte[] expResult = bb.array();
            byte[] result = CryptoService.toByteArray(value);
            assertArrayEquals(expResult, result);
        }
    }

    /**
     * Test of fromByteArray method, of class CryptoService.
     */
    @org.junit.Test
    public void testFromByteArray() {
        showHeader("fromByteArray");
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            int expResult = random.nextInt();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(expResult);
            byte[] value = bb.array();
            int result = CryptoService.fromByteArray(value);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of generateKey method, of class CryptoService.
     */
    @org.junit.Test
    public void testGenerateKey() throws Exception {
        showHeader("generateKey");
        String[] passwords = {
            "dipu",
            "sudipto",
            "WhatANiceDay",
            "a very long Key with Spacesss"
        };
        CryptoService instance = CryptoService.getDefault();
        for (String password : passwords) {
            Key result = instance.generateKey(password);
            assertNotNull(result);
            assertEquals("AES", result.getAlgorithm());
            assertEquals("RAW", result.getFormat());
            assertEquals(32, result.getEncoded().length);
        }
    }

    /**
     * Test of generateParamSpec method, of class CryptoService.
     */
    @org.junit.Test
    public void testGenerateParamSpec() throws Exception {
        showHeader("generateParamSpec");
        String[] seeds = {
            "dipu",
            "sudipto",
            "WhatANiceDay",
            "a very long Key with Spacesss"
        };
        String[] expResults = {
            "dXBpZHVwaWRkaXB1ZGlwdQ==",
            "b3RwaWR1c290cGlkdXNzdQ==",
            "eWFEZWNpTkF0YWhXV2hhdA==",
            "c3NzZWNhcFMgaHRpdyB5ZQ=="
        };
        CryptoService instance = CryptoService.getDefault();
        for (int i = 0; i < seeds.length; ++i) {
            String seed = seeds[i];
            String expResult = expResults[i];
            IvParameterSpec result = (IvParameterSpec) instance.generateParamSpec(seed);
            assertNotNull(result);
            assertEquals(16, result.getIV().length);
            assertArrayEquals(result.getIV(), CryptoService.getPasswordBlock(seed, 16));
            assertEquals(expResult, Base64.getEncoder().encodeToString(result.getIV()));
        }
    }

    /**
     * Test of generateKeyPair method, of class CryptoService.
     *
     * @throws java.lang.Exception
     */
    @org.junit.Test
    public void testGenerateKeyPair4096() throws Exception {
        showHeader("generateKeyPair4096");
        int bitSize = 4096;
        CryptoService instance = CryptoService.getDefault();
        KeyPair result = instance.generateKeyPair(bitSize);
        assertNotNull(result);
        assertNotNull(result.getPublic());
        assertNotNull(result.getPrivate());
        assertEquals("RSA", result.getPublic().getAlgorithm());
        assertEquals("RSA", result.getPrivate().getAlgorithm());
        assertEquals("X.509", result.getPublic().getFormat());
        assertEquals("PKCS#8", result.getPrivate().getFormat());
    }

    /**
     * Test of generateKeyPair method, of class CryptoService.
     *
     * @throws java.lang.Exception
     */
    //@org.junit.Test
    public void testGenerateKeyPair2048() throws Exception {
        showHeader("generateKeyPair2048");
        int bitSize = 2048;
        CryptoService instance = CryptoService.getDefault();
        KeyPair result = instance.generateKeyPair(bitSize);
        assertNotNull(result);
        assertNotNull(result.getPublic());
        assertNotNull(result.getPrivate());
        assertEquals("RSA", result.getPublic().getAlgorithm());
        assertEquals("RSA", result.getPrivate().getAlgorithm());
        assertEquals("X.509", result.getPublic().getFormat());
        assertEquals("PKCS#8", result.getPrivate().getFormat());
    }

    /**
     * Test of generateX500Name method, of class CryptoService.
     */
    @org.junit.Test
    public void testGenerateX500Name() {
        showHeader("generateX500Name");
        String alias = "Test";
        String clientName = "";
        String clientEmail = "dipu.sudipta@gmail.com";
        String organizationalUnit = "Algo U";
        String organization = "Org";
        String countryCode = null;
        CryptoService instance = CryptoService.getDefault();
        X500Name result = instance.generateX500Name(alias, clientName, clientEmail, organizationalUnit, organization, countryCode);
        assertNotNull(result);
        ArrayList<String> rvals = new ArrayList<>();
        for (RDN rdn : result.getRDNs()) {
            for (AttributeTypeAndValue tav : rdn.getTypesAndValues()) {
                rvals.add(tav.getValue().toString());
                System.out.println(tav.getType() + " = " + tav.getValue());
            }
        }
        assertEquals(alias, rvals.get(0));
        assertEquals(clientEmail, rvals.get(1));
        assertEquals(organization, rvals.get(2));
        assertEquals(organizationalUnit, rvals.get(3));
    }

    /**
     * Test of generateSelfSignedX509Certificate method, of class CryptoService.
     */
    @org.junit.Test
    public void testGenerateSelfSignedX509Certificate() throws Exception {
        showHeader("generateSelfSignedX509Certificate");
        String alias = "Test";
        String clientName = "";
        String clientEmail = "dipu.sudipta@gmail.com";
        String organizationalUnit = "Algo U";
        String organization = "Org";
        String countryCode = null;
        CryptoService instance = CryptoService.getDefault();
        KeyPair keyPair = instance.generateKeyPair(4096);
        X500Name subject = instance.generateX500Name(alias, clientName, clientEmail, organizationalUnit, organization, countryCode);

        X509Certificate result = instance.generateSelfSignedX509Certificate(keyPair, subject);
        assertNotNull(result);
        result.checkValidity();
        assertEquals(2147483647, result.getBasicConstraints());
        assertEquals(3, result.getVersion());
        assertEquals(new Date().getYear(), result.getNotBefore().getYear());
        assertEquals(100 + new Date().getYear(), result.getNotAfter().getYear());
        assertEquals("SHA256WITHRSA", result.getSigAlgName());
        assertEquals("1.2.840.113549.1.1.11", result.getSigAlgOID());

        Certificate cert = result;
        assertArrayEquals(cert.getPublicKey().getEncoded(), keyPair.getPublic().getEncoded());
        assertEquals("RSA", cert.getPublicKey().getAlgorithm());
        assertEquals("X.509", cert.getPublicKey().getFormat());
    }

}
