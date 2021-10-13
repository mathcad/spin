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
 * This classes define how the Hanyu Pinyin should be outputted.
 *
 * <p>
 * The output feature includes:
 * <ul>
 * <li>Output format of character 'ü';
 * <li>Output format of Chinese tones;
 * <li>Cases of letters in outputted string
 * </ul>
 *
 * <p>
 * Default values of these features are listed below:
 *
 * <p>
 * HanyuPinyinVCharType := WITH_U_AND_COLON <br>
 * HanyuPinyinCaseType := LOWERCASE <br>
 * HanyuPinyinToneType := WITH_TONE_NUMBER <br>
 *
 * <p>
 * <b>Some combinations of output format options are meaningless. For example,
 * WITH_TONE_MARK and WITH_U_AND_COLON.</b>
 *
 * <p>
 * The combination of different output format options are listed below. For
 * example, '吕'
 *
 * <table border="1">
 * <caption>Examples</caption>
 * <tr>
 * <th colspan="4"> LOWERCASE </th>
 * </tr>
 * <tr>
 * <th>Combination</th>
 * <th>WITH_U_AND_COLON</th>
 * <th>WITH_V</th>
 * <th>WITH_U_UNICODE</th>
 * </tr>
 * <tr>
 * <th>WITH_TONE_NUMBER</th>
 * <td>lu:3</td>
 * <td>lv3</td>
 * <td>lü3</td>
 * </tr>
 * <tr>
 * <th>WITHOUT_TONE</th>
 * <td>lu:</td>
 * <td>lv</td>
 * <td>lü</td>
 * </tr>
 * <tr>
 * <th>WITH_TONE_MARK</th>
 * <td>throw exception</td>
 * <td>throw exception</td>
 * <td>lǚ</td>
 * </tr>
 * </table>
 *
 * <table border="1">
 * <caption>Examples</caption>
 * <tr>
 * <th colspan="4"> UPPERCASE </th>
 * </tr>
 * <tr>
 * <th>Combination</th>
 * <th>WITH_U_AND_COLON</th>
 * <th>WITH_V</th>
 * <th>WITH_U_UNICODE</th>
 * </tr>
 * <tr>
 * <th>WITH_TONE_NUMBER</th>
 * <td>LU:3</td>
 * <td>LV3</td>
 * <td>LÜ3</td>
 * </tr>
 * <tr>
 * <th>WITHOUT_TONE</th>
 * <td>LU:</td>
 * <td>LV</td>
 * <td>LÜ</td>
 * </tr>
 * <tr>
 * <th>WITH_TONE_MARK</th>
 * <td>throw exception</td>
 * <td>throw exception</td>
 * <td>LǙ</td>
 * </tr>
 * </table>
 *
 * @author Li Min (xmlerlimin@gmail.com)
 * @see PinyinVCharType
 * @see PinyinCaseType
 * @see PinyinToneType
 */
public final class PinyinOutputFormat {
    private PinyinVCharType vCharType;
    private PinyinCaseType caseType;
    private PinyinToneType toneType;

    public PinyinOutputFormat() {
        restoreDefault();
    }

    private static final PinyinOutputFormat DEFAULT = new PinyinOutputFormat();
    private static final PinyinOutputFormat WITHOUT_TONE = new PinyinOutputFormat().setToneType(PinyinToneType.WITHOUT_TONE)
        .setVCharType(PinyinVCharType.WITH_V);

    public static PinyinOutputFormat getDefault() {
        return DEFAULT;
    }

    public static PinyinOutputFormat withoutTone() {
        return WITHOUT_TONE;
    }

    public static PinyinOutputFormat create() {
        return new PinyinOutputFormat();
    }

    /**
     * Restore default variable values for this class
     * <p>
     * Default values are listed below:
     *
     * <p>
     * HanyuPinyinVCharType := WITH_U_AND_COLON <br>
     * HanyuPinyinCaseType := LOWERCASE <br>
     * HanyuPinyinToneType := WITH_TONE_NUMBER <br>
     *
     * @return instance
     */
    public PinyinOutputFormat restoreDefault() {
        vCharType = PinyinVCharType.WITH_U_AND_COLON;
        caseType = PinyinCaseType.LOWERCASE;
        toneType = PinyinToneType.WITH_TONE_NUMBER;
        return this;
    }

    /**
     * Returns the output cases of Hanyu Pinyin characters
     *
     * @return caseType
     * @see PinyinCaseType
     */
    public PinyinCaseType getCaseType() {
        return caseType;
    }

    /**
     * Define the output cases of Hanyu Pinyin characters
     *
     * @param caseType the output cases of Hanyu Pinyin characters
     * @return instance
     * @see PinyinCaseType
     */
    public PinyinOutputFormat setCaseType(PinyinCaseType caseType) {
        this.caseType = caseType;
        return this;
    }

    /**
     * Returns the output format of Chinese tones
     *
     * @return toneType
     * @see PinyinToneType
     */
    public PinyinToneType getToneType() {
        return toneType;
    }

    /**
     * Define the output format of Chinese tones
     *
     * @param toneType the output format of Chinese tones
     * @return instance
     * @see PinyinToneType
     */
    public PinyinOutputFormat setToneType(PinyinToneType toneType) {
        this.toneType = toneType;
        return this;
    }

    /**
     * Returns output format of character 'ü'
     *
     * @return charType
     * @see PinyinVCharType
     */
    public PinyinVCharType getVCharType() {
        return vCharType;
    }

    /**
     * Define the output format of character 'ü'
     *
     * @param charType the output format of character 'ü'
     * @return instance
     * @see PinyinVCharType
     */
    public PinyinOutputFormat setVCharType(PinyinVCharType charType) {
        vCharType = charType;
        return this;
    }


}
