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
package org.dpulab.hideaway.models.objecttable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this to set CSS styles of column data (limited).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TableColumnStyle {

    /**
     * List font style. <br>
     * Available items:<br>
     * <ul>
     * <li>Font.PLAIN = 0</li>
     * <li>Font.BOLD = 1</li>
     * <li>Font.ITALIC = 2</li>
     * <li>Font.BOLD | Font.ITALIC = 3</li>
     * </ul>
     */
    int fontStyle();

    /**
     * Horizontal alignment of the column.  <br>
     * Available items:<br>
     * <ul>
     * <li>JLabel.CENTER= 0</li>
     * <li>JLabel.LEADING= 10</li>
     * <li>JLabel.TRAILING= 11</li>
     * </ul>
     */
    int align();

    /**
     * The foreground color
     *
     * @return
     */
    String color();
}
