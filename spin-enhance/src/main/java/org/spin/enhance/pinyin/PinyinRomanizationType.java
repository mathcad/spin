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
 * The class describes variable Chinese Pinyin Romanization System
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
public enum PinyinRomanizationType {
    /**
     * Hanyu Pinyin system
     */
    HANYU_PINYIN("hanyu"),

    /**
     * Wade-Giles Pinyin system
     */
    WADEGILES_PINYIN("wade"),

    /**
     * Mandarin Phonetic Symbols 2 (MPS2) Pinyin system
     */
    MPS2_PINYIN("mpsii"),

    /**
     * Yale Pinyin system
     */
    YALE_PINYIN("yale"),

    /**
     * Tongyong Pinyin system
     */
    TONGYONG_PINYIN("tongyong"),

    /**
     * Gwoyeu Romatzyh system
     */
    GWOYEU_ROMATZYH("gwoyeu");

    private String tagName;

    /**
     * Constructor
     */
    PinyinRomanizationType(String tagName) {
        this.tagName = tagName;

    }

    /**
     * @return Returns the tagName.
     */
    public String getTagName() {
        return tagName;
    }
}
