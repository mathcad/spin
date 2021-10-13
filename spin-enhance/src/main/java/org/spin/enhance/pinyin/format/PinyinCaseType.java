/*
 * This file is part of pinyin4j (http://sourceforge.net/projects/pinyin4j/) and distributed under
 * GNU GENERAL PUBLIC LICENSE (GPL).
 *
 * pinyin4j is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * pinyin4j is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with pinyin4j.
 */

package org.spin.enhance.pinyin.format;

/**
 * Define the output case of Hanyu Pinyin string
 *
 * <p>
 * This class provides several options for outputted cases of Hanyu Pinyin
 * string, which are listed below. For example, Chinese character '民'
 * </p>
 *
 * <table>
 * <caption>Examples</caption>
 * <tr>
 * <th>Options</th>
 * <th>Output</th>
 * </tr>
 * <tr>
 * <td>LOWERCASE</td>
 * <td>min2</td>
 * </tr>
 * <tr>
 * <td>UPPERCASE</td>
 * <td>MIN2</td>
 * </tr>
 * </table>
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
public enum PinyinCaseType {

    /**
     * The option indicates that hanyu pinyin is outputted as uppercase letters
     */
    UPPERCASE,

    /**
     * The option indicates that hanyu pinyin is outputted as lowercase letters
     */
    LOWERCASE,
    ;
}
