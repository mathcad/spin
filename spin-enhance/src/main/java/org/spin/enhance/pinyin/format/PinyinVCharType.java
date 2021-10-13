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
 * Define the output format of character 'ü'
 *
 * <p>
 * 'ü' is a special character of Hanyu Pinyin, which can not be simply
 * represented by English letters. In Hanyu Pinyin, such characters include 'ü',
 * 'üe', 'üan', and 'ün'.
 *
 * <p>
 * This class provides several options for output of 'ü', which are listed
 * below.
 *
 * <table>
 * <caption>Examples</caption>
 * <tr>
 * <th>Options</th>
 * <th>Output</th>
 * </tr>
 * <tr>
 * <td>WITH_U_AND_COLON</td>
 * <td>u:</td>
 * </tr>
 * <tr>
 * <td>WITH_V</td>
 * <td>v</td>
 * </tr>
 * <tr>
 * <td>WITH_U_UNICODE</td>
 * <td>ü</td>
 * </tr>
 * </table>
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
public enum PinyinVCharType {

    /**
     * The option indicates that the output of 'ü' is "u:"
     */
    WITH_U_AND_COLON,

    /**
     * The option indicates that the output of 'ü' is "v"
     */
    WITH_V,

    /**
     * The option indicates that the output of 'ü' is "ü" in Unicode form
     */
    WITH_U_UNICODE,
    ;
}
