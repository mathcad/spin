package org.spin.core.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import net.sourceforge.pinyin4j.multipinyin.MultiPinyinConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PinyinUtils {

    static {
        MultiPinyinConfig.multiPinyinPath = "D:\\user.txt";
    }
    private PinyinUtils() {
    }

    /**
     * 根据汉字获得此汉字的拼音
     *
     * @param chinese 中文字符串
     * @return 拼音字符串
     */
    public static String getPinYin(String chinese) {
        return getPinYin(chinese, false);
    }

    /**
     * 根据汉字获得此汉字的拼音首字母
     *
     * @param chinese 中文字符串
     * @return 每个字的拼音首字母字符串
     */
    public static String getPinYinHeadChar(String chinese) {
        return getPinYin(chinese, true);
    }

    private static String getPinYin(String chinese, boolean isHeadChar) {
        int len = chinese.length();
        char[] hanzi = chinese.toCharArray();

        // 设置输出格式
        HanyuPinyinOutputFormat formatParam = new HanyuPinyinOutputFormat();
        formatParam.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        formatParam.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder py = new StringBuilder();
        Pattern pattern = Pattern.compile("^[\u4e00-\u9fa5]{0,128}$");
        for (int i = 0; i < len; i++) {
            char c = hanzi[i];
            Matcher matcher = pattern.matcher(String.valueOf(c));
            // 检查是否是汉字,如果不是汉字就不转换
            if (!matcher.matches()) {
                py.append(c);
                continue;
            }
            // 对汉字进行转换成拼音
            try {
                String[] t2 = PinyinHelper.toHanyuPinyinStringArray(c,
                    formatParam);
                if (isHeadChar) {
                    py.append(t2[0].charAt(0));
                } else {
                    py.append(t2[0]);
                }

            } catch (BadHanyuPinyinOutputFormatCombination e) {
                py.append(c);
            }
        }
        return py.toString();
    }
}
