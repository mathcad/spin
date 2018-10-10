package org.spin.core.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/10.</p>
 *
 * @author xuweinan
 */
class PinyinUtilsTest {

    @Test
    void getPinYinHeadChar() {
        System.out.println(PinyinUtils.getPinYinHeadChar("长安铃木"));
    }

    @Test
    void getPinYinHeadChar2() throws BadHanyuPinyinOutputFormatCombination {
        System.out.println(PinyinUtils.getPinYinHeadChar("长安铃木"));
        HanyuPinyinOutputFormat formatParam = new HanyuPinyinOutputFormat();
        formatParam.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        formatParam.setVCharType(HanyuPinyinVCharType.WITH_V);
        String a = PinyinHelper.toHanYuPinyinString("长安铃木你是大笨蛋", formatParam, ",", true);
        System.out.println(a);

        String[] t2 = PinyinHelper.toHanyuPinyinStringArray('长',
            formatParam);
        System.out.println(Arrays.toString(t2));
    }
}
