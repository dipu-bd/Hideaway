/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models.objecttable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this to set the column width. If empty or not used, the column name will
 * be calculated from the field name.
 *
 * @author dipu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TableColumnInfo {

    /**
     * Display name of the column.
     *
     * @return
     */
    String name();

    /**
     * Minimum width.
     *
     * @return
     */
    int min();

    /**
     * Maximum width.
     *
     * @return
     */
    int max();

    /**
     * Preferred width.
     *
     * @return
     */
    int prefer();

    /**
     * Is the field editable
     *
     * @return
     */
    boolean editable();
}
