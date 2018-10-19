package org.spin.enhance.pinyin;

import org.junit.jupiter.api.Test;
import org.spin.enhance.pinyin.format.HanyuPinyinOutputFormat;
import org.spin.enhance.pinyin.format.HanyuPinyinToneType;
import org.spin.enhance.pinyin.format.HanyuPinyinVCharType;

/**
 * Created by 刘一波 on 16/3/4.
 * E-Mail:yibo.liu@tqmall.com
 */
public class Q {
    static HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();

    static {
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    @Test
    public void testMulti() throws Exception {
//        MultiPinyinConfig.addMultiPinyinPath("classpath:///my_multi_pinyin.txt");
        System.out.println(PinyinHelper.toHanYuPinyinString("呵呵...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("吸血鬼...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("吸血鬼日记...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("我还要去图书馆还书...", outputFormat, "", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("一五一十", outputFormat, "", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("女医明妃传", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("一人做事一人当还", outputFormat, ".", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("梦之安魂曲", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长春..", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长春不老/", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("刘一波", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长安铃木", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长城", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("蚌埠", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("六安", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长寿", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长命", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长空", outputFormat, ",", false, false));

        System.out.println(PinyinHelper.toHanYuPinyinString("长安aaaa铃sdf12木", ",", true));
        System.out.println(PinyinHelper.toHanYuPinyinString("长安aaaa铃sdf12木", "", true));
        System.out.println(PinyinHelper.toHanYuPinyinString("长安aaaa铃sdf12木", ",", false));
        System.out.println(PinyinHelper.toHanYuPinyinString("长安aaaa铃sdf12木", "", false));
        System.out.println(PinyinHelper.toMPS2PinyinStringArray('奥')[0]);
    }
}
