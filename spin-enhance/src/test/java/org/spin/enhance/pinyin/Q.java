package org.spin.enhance.pinyin;

import org.junit.jupiter.api.Test;
import org.spin.enhance.pinyin.format.PinyinOutputFormat;
import org.spin.enhance.pinyin.format.PinyinToneType;
import org.spin.enhance.pinyin.format.PinyinVCharType;

/**
 * Created by 刘一波 on 16/3/4.
 * E-Mail:yibo.liu@tqmall.com
 */
public class Q {
    static PinyinOutputFormat outputFormat = new PinyinOutputFormat();

    static {
        outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);
        outputFormat.setVCharType(PinyinVCharType.WITH_V);
    }

    @Test
    public void testMulti() throws Exception {
//        MultiPinyinConfig.addMultiPinyinPath("classpath:///my_multi_pinyin.txt");
        System.out.println(PinyinHelper.toPinyinString("呵呵...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toPinyinString("吸血鬼...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toPinyinString("吸血鬼日记...", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toPinyinString("我还要去图书馆还书...", outputFormat, "", true, false));
        System.out.println(PinyinHelper.toPinyinString("一五一十", outputFormat, "", true, false));
        System.out.println(PinyinHelper.toPinyinString("女医明妃传", outputFormat, ";", true, false));
        System.out.println(PinyinHelper.toPinyinString("一人做事一人当还", outputFormat, ".", true, false));
        System.out.println(PinyinHelper.toPinyinString("梦之安魂曲", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toPinyinString("长春..", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toPinyinString("长春不老/", outputFormat, ",", true, false));
        System.out.println(PinyinHelper.toPinyinString("刘一波", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("长安铃木", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("长城", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("蚌埠", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("六安", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("长寿", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("长命", outputFormat, ",", false, false));
        System.out.println(PinyinHelper.toPinyinString("长空", outputFormat, ",", false, false));

        System.out.println(PinyinHelper.toPinyinString("长安aaaa铃sdf12木", ",", true));
        System.out.println(PinyinHelper.toPinyinString("长安aaaa铃sdf12木", "", true));
        System.out.println(PinyinHelper.toPinyinString("长安aaaa铃sdf12木", ",", false));
        System.out.println(PinyinHelper.toPinyinString("长安aaaa铃sdf12木", "", false));

        PinyinOutputFormat formatParam = new PinyinOutputFormat();
        formatParam.setToneType(PinyinToneType.WITHOUT_TONE);
        formatParam.setVCharType(PinyinVCharType.WITH_V);
        System.out.println(PinyinHelper.toPinyinStringList('和'));
        System.out.println(PinyinHelper.toMPS2PinyinStringList('奥').get(0));


//        String name = "长六和当还";
        String name = "长还六";
        StringBuilder stringBuilder = PinyinHelper.splitStrToMultiPinyin(name, StringBuilder::new, (s, i, n) -> s.append(",").append(n), PinyinOutputFormat.withoutTone());
        System.out.println(stringBuilder.substring(1));
    }

    private static final PinyinOutputFormat formatParam = new PinyinOutputFormat();

    static {
        formatParam.setToneType(PinyinToneType.WITHOUT_TONE);
        formatParam.setVCharType(PinyinVCharType.WITH_V);
    }



}

/*
chang zhang
liu   lu
he    he huo huo huo hai he hu
dang  tang
hai   huan
 */
