/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dpulab.hideaway.models.objecttable.TableColumn;
import org.dpulab.hideaway.models.objecttable.TableColumnInfo;
import org.dpulab.hideaway.models.objecttable.TableColumnStyle;
import org.junit.*;
import org.junit.Assert.*;

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
    public TableColumn[] testColumnEntries() {
        Class clazz = IndexEntryModel.class;
        ArrayList<TableColumn> columns = new ArrayList<>();
        List<AccessibleObject> list = Arrays.asList(clazz.getFields());
        list.addAll(Arrays.asList(clazz.getMethods()));
        for (AccessibleObject f : list) {
            TableColumnInfo info = f.getAnnotation(TableColumnInfo.class);
            TableColumnStyle style = f.getAnnotation(TableColumnStyle.class);
            if (info != null || style != null) {
                TableColumn col = TableColumn.build(f);
                columns.add(col);
                System.out.printf(" field = %s, name = %s\n", col.getColumnName(), col.getFieldName());
            }
        }
        return columns.toArray(new TableColumn[0]);
    }

    @Test
    public void testFieldReflection() {
        System.out.println(StringUtils.center(" Test Field Reflection ", 60, '-'));
        IndexEntryModel entry = new IndexEntryModel();
        entry.fileName = "entry.sample";
        entry.fileSize = 80105325;
        for (TableColumn col : testColumnEntries()) {
            System.out.printf("* %s = %s\n", col.getColumnName(), col.extractValue(entry));
        }
    }

}
