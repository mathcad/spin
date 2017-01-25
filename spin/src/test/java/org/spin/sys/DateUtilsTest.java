package org.spin.sys;

import org.junit.Test;
import org.spin.util.DateUtils;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertTrue;

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

    @Test
    public void testJava8DateTime() {
        LocalTime time = LocalTime.now();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
//        System.out.println(time.format(formatter));
        Time t = Time.valueOf(time);
        System.out.println(sdf.format(t));
        assertTrue(true);
    }
}