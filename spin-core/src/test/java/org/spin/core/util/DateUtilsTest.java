package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2016/9/21.
 */
public class DateUtilsTest {
    @Test
    public void parseDate() {
        String date = "星期二 十月 13 22:23:54 CST 2016";
//        String patten = "(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])(.+)([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)";
//        Pattern p = Pattern.compile(patten);
//        Matcher m = p.matcher(date);
//        System.out.println(m.matches());
        System.out.println(DateUtils.toDate(date));
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

    @Test
    public void testDateCalc() {
        LocalDateTime dateTime = LocalDateTime.now();
        System.out.println(dateTime);
        dateTime = DateUtils.addMonths(dateTime, -3);
        System.out.println(dateTime);
        System.out.println(DateUtils.formatDateForSecond(DateUtils.toLocalDateTime("2016/11-05啊10:05:52.326")));
        assertTrue(true);
    }

    @Test
    public void testFormat() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = "2016-1-5 10:5:52";
        System.out.println(sdf.parse(date));
        System.out.println(DateUtils.toDate(date));
    }

    @Test
    public void toDate() {
        System.out.println(DateUtils.toDate("2017-5-23"));
        System.out.println(DateUtils.toLocalDateTime("2017年05-23"));
    }

    @Test
    public void testDuration() {
        Date dateTime = new Date();
        Date end = DateUtils.addHours(dateTime, 4);
        end = DateUtils.addMinutes(end, 31);
//        end = DateUtils.addYears(end, 2);
        System.out.println(DateUtils.prettyDuration(end, dateTime));
    }

    @Test
    public void testUtc() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        System.out.println(dtf.format(ZonedDateTime.now()));
        ZonedDateTime z = ZonedDateTime.parse("2018-03-12T01:11:16.912Z", dtf);

        Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[A-Z]+");
        System.out.println(p.matcher("2018-03-12T01:11:16.912CTS").matches());
        System.out.println(z.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());

        System.out.println(DateUtils.toLocalDateTime("2018-03-12T01:11:16.912Asia/Shanghai"));
    }

    @Test
    public void testSame() {
        System.out.println(DateUtils.formatDateForSecond(new Date(System.currentTimeMillis() / 86400_000L * 86400_00L)));
        System.out.println(DateUtils.isSameDay(LocalDate.now(), DateUtils.toLocalDateTime("2019-02-25 00:12:47")));
    }

    @Test
    void testDate() throws ParseException {
        System.out.println(DateUtils.toLocalDateTime("2021-11-10 17:02:05"));
        System.out.println(DateUtils.toLocalDateTime("2021-11-10 17:02"));
        System.out.println(DateUtils.toLocalDateTime("2021-11-10"));

        Date startTime = DateUtils.toDate("2021-11-10 17:02");
        Date endTime = DateUtils.toDate("2021-11-10 00:00:00");
        System.out.println(DateUtils.isSameDay(startTime, endTime));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = sdf.parse("2021-11-10 17:02");
        System.out.println(parse);
    }

    public static class A {
        LocalDate date;
    }
}
