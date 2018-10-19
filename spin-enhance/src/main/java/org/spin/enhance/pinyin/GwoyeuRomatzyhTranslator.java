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

package org.spin.enhance.pinyin;


/**
 * A class contains logic that translates from Hanyu Pinyin to Gwoyeu Romatzyh
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
class GwoyeuRomatzyhTranslator {
    /**
     * @param hanyuPinyinStr Given unformatted Hanyu Pinyin with tone number
     * @return Corresponding Gwoyeu Romatzyh; null if no mapping is found.
     */
    static String convertHanyuPinyinToGwoyeuRomatzyh(String hanyuPinyinStr) {
        String pinyinString = TextHelper.extractPinyinString(hanyuPinyinStr);
        String toneNumberStr = TextHelper.extractToneNumber(hanyuPinyinStr);

        // return value
        return PinyinMapping.getGwoyeuItems().get(PinyinRomanizationType.HANYU_PINYIN.getTagName()).get(pinyinString)
            .getValue(PinyinRomanizationType.GWOYEU_ROMATZYH.getTagName() + tones[Integer.parseInt(toneNumberStr)]);
    }

    /**
     * The postfixs to distinguish different tone of Gwoyeu Romatzyh
     *
     * <i>Should be removed if new xPath parser supporting tag name with number.</i>
     */
    static private String[] tones = new String[]{"", "I", "Ii", "Iii", "Iv", "V"};
}
