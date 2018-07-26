/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import org.apache.commons.lang3.StringUtils;
import org.dpulab.hideaway.models.objecttable.TableColumnInfo;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author dipu
 */
public class IndexEntryModelTest {

    public IndexEntryModelTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println(StringUtils.center(" IndexEntry ", 80, "="));
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println(StringUtils.center("", 80, "-"));
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testColumnEntries() {
        TableColumnInfo[] columns = TableColumnInfo.build(IndexEntryModel.class);
        assertNotNull(columns);
        assertTrue(columns.length > 0);
    }

    @Test
    public void testFieldReflection() {
        System.out.println(StringUtils.center(" Test Field Reflection ", 60, '-'));
        IndexEntryModel entry = new IndexEntryModel();
        entry.fileName = "My Sample Entry";
        entry.fileSize = 80105325;
        TableColumnInfo[] items = TableColumnInfo.build(IndexEntryModel.class);
        for (TableColumnInfo col : items) {
            System.out.printf("* %s = %s (edit: %s, update: %s, style: %s)\n",
                    col.getName(), col.extractValue(entry), col.isEditable(), col.canUpdate(), col.getStyle());
        }
    }

}
