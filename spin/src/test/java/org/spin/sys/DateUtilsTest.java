package org.spin.sys;

import org.spin.util.DateUtils;
import org.junit.Test;

/**
 * Created by Arvin on 2016/9/21.
 */
public class DateUtilsTest {
    @Test
    public void parseDate() throws Exception {
        String date = "星期二 十月 13 22:23:54 CST 2016";
//        String patten = "(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])(.+)([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)";
//        Pattern p = Pattern.compile(patten);
//        Matcher m = p.matcher(date);
//        System.out.println(m.matches());
        System.out.println(DateUtils.parseDate(date));
    }

}