/*
 * This file is part of pinyin4j (http://sourceforge.net/projects/pinyin4j/) and distributed under
 * GNU GENERAL PUBLIC LICENSE (GPL).
 * <p>
 * pinyin4j is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * pinyin4j is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with pinyin4j.
 */

package org.spin.enhance.pinyin;

import org.spin.core.function.TripleConsumer;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.enhance.ip.Util;
import org.spin.enhance.pinyin.format.PinyinOutputFormat;
import org.spin.enhance.pinyin.format.exception.BadPinyinOutputFormatCombination;
import org.spin.enhance.pinyin.multipinyin.Trie;

import java.util.*;
import java.util.function.Supplier;

/**
 * A class provides several utility functions to convert Chinese characters
 * (both Simplified and Tranditional) into various Chinese Romanization
 * representations
 *
 * @author Li Min (xmlerlimin@gmail.com)
 */
public final class PinyinHelper extends Util {

    /**
     * Get all unformmatted Hanyu Pinyin presentations of a single Chinese
     * character (both Simplified and Tranditional)
     * <p>
     * For example, <br> If the input is '间', the return will be an array with
     * two Hanyu Pinyin strings: <br> "jian1" <br> "jian4" <br> <br> If the
     * input is '李', the return will be an array with single Hanyu Pinyin
     * string: <br> "li3"
     * </p>
     * <p>
     * <b>Special Note</b>: If the return is "none0", that means the input
     * Chinese character exists in Unicode CJK talbe, however, it has no
     * pronounciation in Chinese
     * </p>
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted Hanyu Pinyin
     * presentations with tone numbers; null for non-Chinese character
     */
    public static List<String> toPinyinStringList(char ch) {
        return getUnformattedHanyuPinyinStringList(ch);
    }

    /**
     * Get all Hanyu Pinyin presentations of a single Chinese character (both
     * Simplified and Tranditional)
     * <p>
     * For example, <br> If the input is '间', the return will be an array with
     * two Hanyu Pinyin strings: <br> "jian1" <br> "jian4" <br> <br> If the
     * input is '李', the return will be an array with single Hanyu Pinyin
     * string: <br> "li3"
     * </p>
     * <p>
     * <b>Special Note</b>: If the return is "none0", that means the input
     * Chinese character is in Unicode CJK talbe, however, it has no
     * pronounciation in Chinese
     * </p>
     *
     * @param ch           the given Chinese character
     * @param outputFormat describes the desired format of returned Hanyu Pinyin String
     * @return a String array contains all Hanyu Pinyin presentations with tone
     * numbers; return empty string for non-Chinese character
     * @throws BadPinyinOutputFormatCombination if certain combination of output formats happens
     * @see PinyinOutputFormat
     * @see BadPinyinOutputFormatCombination
     */
    public static List<String> toPinyinStringList(char ch, PinyinOutputFormat outputFormat)
        throws BadPinyinOutputFormatCombination {
        return getFormattedPinyinStringList(ch, outputFormat);
    }

    /**
     * Return the formatted Hanyu Pinyin representations of the given Chinese
     * character (both in Simplified and Tranditional) in array format.
     *
     * @param ch           the given Chinese character
     * @param outputFormat Describes the desired format of returned Hanyu Pinyin string
     * @return The formatted Hanyu Pinyin representations of the given codepoint
     * in array format; null if no record is found in the hashtable.
     */
    private static List<String> getFormattedPinyinStringList(char ch,
                                                             PinyinOutputFormat outputFormat) throws BadPinyinOutputFormatCombination {
        List<String> pinyinStringList = getUnformattedHanyuPinyinStringList(ch);

        List<String> res = new ArrayList<>(pinyinStringList.size());
        Set<String> c = new HashSet<>(pinyinStringList.size());
        if (CollectionUtils.isNotEmpty(pinyinStringList)) {
            for (String s : pinyinStringList) {
                String t = PinyinFormatter.formatHanyuPinyin(s, outputFormat);
                if (!c.contains(t)) {
                    c.add(t);
                    res.add(t);
                }
            }
        }
        return res;
    }

    /**
     * Delegate function
     *
     * @param ch the given Chinese character
     * @return unformatted Hanyu Pinyin strings; null if the record is not found
     */
    private static List<String> getUnformattedHanyuPinyinStringList(char ch) {
        return ChineseToPinyinResource.getInstance().getPinyinStringList(ch);
    }

    /**
     * Get all unformmatted Tongyong Pinyin presentations of a single Chinese
     * character (both Simplified and Tranditional)
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted Tongyong Pinyin
     * presentations with tone numbers; null for non-Chinese character
     * @see #toPinyinStringList(char)
     */
    public static List<String> toTongyongPinyinStringList(char ch) {
        return convertToTargetPinyinStringList(ch, PinyinRomanizationType.TONGYONG_PINYIN);
    }

    /**
     * Get all unformmatted Wade-Giles presentations of a single Chinese
     * character (both Simplified and Tranditional)
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted Wade-Giles presentations
     * with tone numbers; null for non-Chinese character
     * @see #toPinyinStringList(char)
     */
    public static List<String> toWadeGilesPinyinStringList(char ch) {
        return convertToTargetPinyinStringList(ch, PinyinRomanizationType.WADEGILES_PINYIN);
    }

    /**
     * Get all unformmatted MPS2 (Mandarin Phonetic Symbols 2) presentations of
     * a single Chinese character (both Simplified and Tranditional)
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted MPS2 (Mandarin Phonetic
     * Symbols 2) presentations with tone numbers; null for non-Chinese
     * character
     * @see #toPinyinStringList(char)
     */
    public static List<String> toMPS2PinyinStringList(char ch) {
        return convertToTargetPinyinStringList(ch, PinyinRomanizationType.MPS2_PINYIN);
    }

    /**
     * Get all unformmatted Yale Pinyin presentations of a single Chinese
     * character (both Simplified and Tranditional)
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted Yale Pinyin
     * presentations with tone numbers; null for non-Chinese character
     * @see #toPinyinStringList(char)
     */
    public static List<String> toYalePinyinStringList(char ch) {
        return convertToTargetPinyinStringList(ch, PinyinRomanizationType.YALE_PINYIN);
    }

    /**
     * @param ch                 the given Chinese character
     * @param targetPinyinSystem indicates target Chinese Romanization system should be
     *                           converted to
     * @return string representations of target Chinese Romanization system
     * corresponding to the given Chinese character in array format;
     * null if error happens
     * @see PinyinRomanizationType
     */
    private static List<String> convertToTargetPinyinStringList(char ch,
                                                                PinyinRomanizationType targetPinyinSystem) {
        List<String> hanyuPinyinStringList = getUnformattedHanyuPinyinStringList(ch);

        if (null != hanyuPinyinStringList) {
            List<String> targetPinyinStringArray = new ArrayList<>(hanyuPinyinStringList.size());
            Set<String> c = new HashSet<>(hanyuPinyinStringList.size());
            for (String s : hanyuPinyinStringList) {
                String t = PinyinRomanizationTranslator
                    .convertRomanizationSystem(s, PinyinRomanizationType.HANYU_PINYIN, targetPinyinSystem);
                if (!c.contains(t)) {
                    targetPinyinStringArray.add(t);
                    c.add(t);
                }
            }

            return targetPinyinStringArray;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get all unformmatted Gwoyeu Romatzyh presentations of a single Chinese
     * character (both Simplified and Tranditional)
     *
     * @param ch the given Chinese character
     * @return a String array contains all unformmatted Gwoyeu Romatzyh
     * presentations with tone numbers; null for non-Chinese character
     * @see #toPinyinStringList(char)
     */
    static public List<String> toGwoyeuRomatzyhStringList(char ch) {
        return convertToGwoyeuRomatzyhStringList(ch);
    }

    /**
     * @param ch the given Chinese character
     * @return Gwoyeu Romatzyh string representations corresponding to the given
     * Chinese character in array format; null if error happens
     * @see PinyinRomanizationType
     */
    public static List<String> convertToGwoyeuRomatzyhStringList(char ch) {
        List<String> hanyuPinyinStringList = getUnformattedHanyuPinyinStringList(ch);

        if (null != hanyuPinyinStringList) {
            List<String> targetPinyinStringList = new ArrayList<>(hanyuPinyinStringList.size());

            for (String s : hanyuPinyinStringList) {
                targetPinyinStringList.add(GwoyeuRomatzyhTranslator.convertHanyuPinyinToGwoyeuRomatzyh(s));
            }

            return targetPinyinStringList;

        } else
            return new ArrayList<>();
    }

    /**
     * Get a string which all Chinese characters are replaced by corresponding
     * main (first) Hanyu Pinyin representation.
     * <p>
     * <b>Special Note</b>: If the return contains "none0", that means that
     * Chinese character is in Unicode CJK talbe, however, it has not
     * pronounciation in Chinese. <b> This interface will be removed in next
     * release. </b>
     * </p>
     * <p>
     * Chinese characters are converted into main (first) Hanyu Pinyin
     * representation
     * </p>
     *
     * @param str          A given string contains Chinese characters
     * @param outputFormat Describes the desired format of returned Hanyu Pinyin string
     * @param separate     The string is appended after a Chinese character (excluding
     *                     the last Chinese character at the end of sentence). <b>Note!
     *                     Separate will not appear after a non-Chinese character</b>
     * @param retain       Retain the characters that cannot be converted into pinyin characters
     * @param isHead       ishead
     * @return a String identical to the original one but all recognizable
     * @throws BadPinyinOutputFormatCombination format error
     */
    public static String toPinyinString(String str, PinyinOutputFormat outputFormat,
                                             String separate, boolean retain, boolean isHead) throws BadPinyinOutputFormatCombination {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        ChineseToPinyinResource resource = ChineseToPinyinResource.getInstance();
        StringBuilder resultPinyinStrBuf = new StringBuilder();

        char[] chars = str.toCharArray();
        boolean needSeprate = false;

        for (int i = 0; i < chars.length; i++) {
            String result = null;//匹配到的最长的结果
            char ch = chars[i];
            Trie currentTrie = resource.getUnicodeToHanyuPinyinTable();
            int success = i;
            int current = i;
            do {
                String hexStr = Integer.toHexString((int) ch).toUpperCase();
                currentTrie = currentTrie.get(hexStr);
                if (currentTrie != null) {
                    if (currentTrie.getPinyin() != null) {
                        result = currentTrie.getPinyin();
                        success = current;
                    }
                    currentTrie = currentTrie.getNextTire();
                }
                current++;
                if (current < chars.length)
                    ch = chars[current];
                else
                    break;
            }
            while (currentTrie != null);

            if (result == null) {//如果在前缀树中没有匹配到，那么它就不能转换为拼音，直接输出或者去掉
                if (retain) {
                    needSeprate = true;
                    resultPinyinStrBuf.append(chars[i]);
                }
            } else {
                List<String> pinyinStrList = resource.parsePinyinString(result);
                if (pinyinStrList != null) {
                    String s;
                    for (String value : pinyinStrList) {
                        if (needSeprate) {
                            resultPinyinStrBuf.append(separate);
                        }
                        s = PinyinFormatter.formatHanyuPinyin(value, outputFormat);
                        resultPinyinStrBuf.append(isHead ? String.valueOf(s.charAt(0)) : s);
                        resultPinyinStrBuf.append(separate);
                        needSeprate = false;
                        if (i == success) {
                            break;
                        }
                    }
                }
            }
            i = success;
        }

        String pinyin = resultPinyinStrBuf.toString();
        return separate.length() > 0 && pinyin.endsWith(separate) ? pinyin.substring(0, pinyin.length() - separate.length()) : pinyin;
    }

    public static String toPinyinString(String str,
                                             String separate, boolean isHead) throws BadPinyinOutputFormatCombination {
        return toPinyinString(str, PinyinOutputFormat.withoutTone(), separate, true, isHead);
    }

    public static String toPinyinHeadString(String str) throws BadPinyinOutputFormatCombination {
        return Optional.ofNullable(toPinyinString(str, PinyinOutputFormat.withoutTone(), "", true, true)).map(it -> it.substring(0, 1)).orElse(null);
    }

    public static String toPinyinHeadString(String str, boolean uppercase) throws BadPinyinOutputFormatCombination {
        String head = Optional.ofNullable(toPinyinString(str, PinyinOutputFormat.withoutTone(), "", true, true)).map(it -> it.substring(0, 1)).orElse(null);
        if (uppercase) {
            return StringUtils.toUpperCase(head);
        }

        return head;
    }

    public static <T> T splitStrToMultiPinyin(String str, Supplier<T> container, TripleConsumer<T, Integer, String> combiner, PinyinOutputFormat... format) {
        T res = null == container ? null : container.get();
        char[] chars = str.toCharArray();
        List<List<String>> charPys = new ArrayList<>(chars.length);
        int cnt = 1;

        for(int i = 0; i < chars.length; ++i) {
            charPys.add(ArrayUtils.isEmpty(format) ? toPinyinStringList(chars[i]) : toPinyinStringList(chars[i], format[0]));
            cnt *= charPys.get(i).size();
        }

        StringBuilder t = new StringBuilder();

        for(int i = 0; i < cnt; ++i) {
            int f = cnt;

            for(int j = 0; j < chars.length; ++j) {
                int b = f;
                f /= charPys.get(j).size();
                t.append((charPys.get(j)).get(i % b / f));
            }

            combiner.accept(res, i, t.toString());
            t.setLength(0);
        }

        return res;
    }

    // ! Hidden constructor
    private PinyinHelper() {
    }
}
