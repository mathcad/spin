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
 * Contains the logic translating among different Chinese Romanization systems
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
class PinyinRomanizationTranslator {
    /**
     * convert the given unformatted Pinyin string from source Romanization
     * system to target Romanization system
     *
     * @param sourcePinyinStr    the given unformatted Pinyin string
     * @param sourcePinyinSystem the Romanization system which is currently used by the given
     *                           unformatted Pinyin string
     * @param targetPinyinSystem the Romanization system that should be converted to
     * @return unformatted Pinyin string in target Romanization system; null if
     * error happens
     */
    static String convertRomanizationSystem(String sourcePinyinStr, PinyinRomanizationType sourcePinyinSystem, PinyinRomanizationType targetPinyinSystem) {
        String pinyinString = TextHelper.extractPinyinString(sourcePinyinStr);
        String toneNumberStr = TextHelper.extractToneNumber(sourcePinyinStr);


        String targetPinyinStrWithoutToneNumber = PinyinMapping.getItems().get(sourcePinyinSystem.getTagName()).get(pinyinString).getValue(targetPinyinSystem.getTagName());

        return targetPinyinStrWithoutToneNumber + toneNumberStr;
    }
}
