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

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dipu
 */
public class GeneralUtilsTest {

    public GeneralUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
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

    /**
     * Test of formatDate method, of class GeneralUtils.
     */
    @Test
    public void testFormatDate_long() {
        System.out.println("formatDate");
        long unixTime = 0L;
        String expResult = "";
        String result = GeneralUtils.formatDate(unixTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatDate method, of class GeneralUtils.
     */
    @Test
    public void testFormatDate_Date() {
        System.out.println("formatDate");
        Date date = null;
        String expResult = "";
        String result = GeneralUtils.formatDate(date);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatDate method, of class GeneralUtils.
     */
    @Test
    public void testFormatDate_Date_String() {
        System.out.println("formatDate");
        Date date = null;
        String formatter = "";
        String expResult = "";
        String result = GeneralUtils.formatDate(date, formatter);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatFileSize method, of class GeneralUtils.
     */
    @Test
    public void testFormatFileSize_long() {
        System.out.println("formatFileSize");
        long size = 0L;
        String expResult = "";
        String result = GeneralUtils.formatFileSize(size);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatFileSize method, of class GeneralUtils.
     */
    @Test
    public void testFormatFileSize_double() {
        System.out.println("formatFileSize");
        double size = 0.0;
        String expResult = "";
        String result = GeneralUtils.formatFileSize(size);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of titleCase method, of class GeneralUtils.
     */
    @Test
    public void testTitleCase_String() {
        System.out.println("titleCase");
        String text = "";
        String expResult = "";
        String result = GeneralUtils.titleCase(text);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of titleCase method.
     */
    @Test
    public void testTitleCase_String_boolean() {
        System.out.println(StringUtils.center("titleCase", 60, '-'));
        String[][] samples = {
            {null, null},
            {"", ""},
            {"a", "A"},
            {"aa", "Aa"},
            {"aaa", "Aaa"},
            {"aC", "A C"},
            {"AC", "AC"},
            {"aCa", "A Ca"},
            {"ACa", "A Ca"},
            {"aCamel", "A Camel"},
            {"anCamel", "An Camel"},
            {"CamelCase", "Camel Case"},
            {"camelCase", "Camel Case"},
            {"snake_case", "Snake Case"},
            {"toCamelCaseString", "To Camel Case String"},
            {"ABDTest", "ABD Test"},
            {"toCAMELCase", "To CAMEL Case"},
            {"title123Case", "Title123 Case"},
            {"expect11", "Expect11"},
            {"all0verMe3", "All0 Ver Me3"},
            {"_under_the_scoreCamelWith_", "Under The Score Camel With"},
            {"___", "___"},
            {"__a__", "A"},
            {"_A_b_c____aa", "A B C Aa"},
            {"_get$It132done", "Get It132 Done"},
            {"_122_", "122"},
            {"_no112", "No112"},
            {"whatSthePoint", "What Sthe Point"},
            {"n0pe_aLoud", "N0 Pe A Loud"},
            {"canHave SpacesThere", "Can Have Spaces There"},
            {"  why_underScore exists  ", "Why Under Score Exists"},
            {"Case-13title", "Case13 Title"},
            {"small-to-be-seen", "Small To Be Seen"},
            {"-no-allow-", "No Allow"},
            {"_paren-_-allow--not!", "Paren Allow Not"},
            {"Other.Allow.--False?", "Other Allow False"},
            {"stripEvery.damn-paren- -_now", "Strip Every Damn Paren Now"},
            {"getMe", "Get Me"}
        };
        for (String[] inp : samples) {
            String out = GeneralUtils.titleCase(inp[0]);
            System.out.printf("Test: '%s' | Expect: '%s' | Found '%s'\n", inp[0], inp[1], out);
            assertEquals(out, inp[1]);
        }

    }

}
