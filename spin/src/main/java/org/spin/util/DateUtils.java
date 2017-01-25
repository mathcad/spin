package org.spin.util;

import org.spin.sys.ErrorAndExceptionCode;
import org.spin.throwable.SimplifiedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期工具类
 *
 * @author xuweinan
 */
public abstract class DateUtils {
    private static final String yearPattern = "(\\d{4})";
    private static final String shortYearPattern = "(\\d{2})";
    private static final String alignMonthPattern = "(0[1-9]|1[0-2])";
    private static final String alignDayPattern = "(0[1-9]|[1-2]\\d|3[0-1])";
    private static final String alignHourPattern = "([0-1]\\d|2[0-3])";
    private static final String alignMinutePattern = "([0-5]\\d)";
    private static final String alignSecondPattern = "([0-5]\\d)";


    private static final String day = "yyyy-MM-dd";
    private static final String second = "yyyy-MM-dd HH:mm:ss";
    private static final String zhDay = "yyyy年MM月dd日";
    private static final String zhSecond = "yyyy年MM月dd日 HH时mm分ss秒";
    private static final String fullDay = "yyyy_MM_dd_HH_mm_ss_S";
    private static final String noFormat = "yyyyMMddHHmmss";

    private static final SimpleDateFormat daySdf = new SimpleDateFormat(day);
    private static final SimpleDateFormat secondSdf = new SimpleDateFormat(second);
    private static final SimpleDateFormat zhDaySdf = new SimpleDateFormat(second);
    private static final SimpleDateFormat zhSecondSdf = new SimpleDateFormat(second);
    private static final SimpleDateFormat fullDaySdf = new SimpleDateFormat(second);
    private static final SimpleDateFormat noFormatSdf = new SimpleDateFormat(second);

    private static final DateTimeFormatter dayDtf = DateTimeFormatter.ofPattern(day);
    private static final DateTimeFormatter secondDtf = DateTimeFormatter.ofPattern(second);
    private static final DateTimeFormatter zhDayDtf = DateTimeFormatter.ofPattern(second);
    private static final DateTimeFormatter zhSecondDtf = DateTimeFormatter.ofPattern(second);
    private static final DateTimeFormatter fullDayDtf = DateTimeFormatter.ofPattern(second);
    private static final DateTimeFormatter noFormatDtf = DateTimeFormatter.ofPattern(second);


    private static final String[] datePatten = {
            yearPattern + "(.)" + alignMonthPattern + "(.)" + alignDayPattern,
    };
    private static final String[] dateFormat = {
            "yyyy{0}MM{1}dd",
    };

    private static final String[] timePatten = {
            alignHourPattern + ":" + alignMinutePattern + ":" + alignSecondPattern,
            alignHourPattern + "时" + alignMinutePattern + "分" + alignSecondPattern + "秒",
            alignHourPattern + ":" + alignMinutePattern,
            alignHourPattern + "时" + alignMinutePattern + "分"
    };

    private static final String[] timeFormat = {
            "HH:mm:ss",
            "HH时mm分ss",
            "HH:mm",
            "HH时mm分",
    };

    private static final Pattern[] pattens = new Pattern[datePatten.length * timePatten.length + datePatten.length];

    static {
        for (int i = 0; i < datePatten.length; i++) {
            pattens[datePatten.length * timePatten.length + i] = Pattern.compile(datePatten[i]);
            for (int j = 0; j < timePatten.length; j++) {
                Pattern pattern = Pattern.compile(datePatten[i] + "(.+)" + timePatten[j]);
                pattens[i * (timePatten.length) + j] = pattern;
            }
        }
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     */
    public static Date toDate(String date) {
        int index = 0;
        Matcher matcher = null;
        while (index != pattens.length) {
            Matcher m = pattens[index].matcher(date);
            if (m.matches()) {
                matcher = m;
                break;
            }
            ++index;
        }
        SimpleDateFormat sdf;
        if (matcher == null)
            sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        else {
            if (index < datePatten.length * timePatten.length)
                sdf = new SimpleDateFormat(StringUtils.format(dateFormat[index / timeFormat.length], matcher.group(2)
                        , matcher.group(4)) + matcher.group(6) + timeFormat[index % timeFormat.length]);
            else
                sdf = new SimpleDateFormat(dateFormat[index % (datePatten.length * timePatten.length)]);
        }
        try {
            return sdf.parse(matcher == null ? date : matcher.group(0));
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     */
    public static LocalDateTime toLocalDateTime(String date) {
        int index = 0;
        Matcher matcher = null;
        while (index != pattens.length) {
            Matcher m = pattens[index].matcher(date);
            if (m.matches()) {
                matcher = m;
                break;
            }
            ++index;
        }
        DateTimeFormatter formatter;
        if (matcher == null)
            formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        else {
            if (index < datePatten.length * timePatten.length)
                formatter = DateTimeFormatter.ofPattern(StringUtils.format(dateFormat[index / timeFormat.length], matcher.group(2)
                        , matcher.group(4)) + matcher.group(6) + timeFormat[index % timeFormat.length]);
            else
                formatter = DateTimeFormatter.ofPattern(dateFormat[index % (datePatten.length * timePatten.length)]);
        }
        try {
            return LocalDateTime.parse(matcher == null ? date : matcher.group(0), formatter);
        } catch (DateTimeParseException e) {
            throw new SimplifiedException(ErrorAndExceptionCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    /**
     * 增加秒
     *
     * @param date    日期
     * @param seconds 秒数
     * @return 结果时间
     */
    public static Date addSeconds(Date date, int seconds) {
        return new Date(date.getTime() + seconds * 1000L);
    }

    /**
     * 增加分
     *
     * @param date    日期
     * @param minutes 分钟数
     * @return 结果时间
     */
    public static Date addMinutes(Date date, int minutes) {
        return new Date(date.getTime() + minutes * 60000L);
    }

    /**
     * 增加小时
     *
     * @param date  日期
     * @param hours 小时数
     * @return 结果时间
     */
    public static Date addHours(Date date, int hours) {
        return new Date(date.getTime() + hours * 3600000L);
    }

    /**
     * 增加天
     *
     * @param date 日期
     * @param days 分钟数
     * @return 结果时间
     */
    public static Date addDays(Date date, int days) {
        return new Date(date.getTime() + days * 86400000L);
    }

    /**
     * 增加周
     *
     * @param date  日期
     * @param weeks 周数
     * @return 结果时间
     */
    public static Date addWeeks(Date date, int weeks) {
        return new Date(date.getTime() + weeks * 604800000L);
    }

    /**
     * 增加月
     *
     * @param date   日期
     * @param months 月数
     * @return 结果时间
     */
    public static Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 增加年
     *
     * @param date  日期
     * @param years 年数
     * @return 结果时间
     */
    public static Date addYears(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     */
    public static String formatDateForFullName(Date date) {
        return fullDaySdf.format(date);
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     */
    public static String formatDateForFullName(TemporalAccessor date) {
        return fullDayDtf.format(date);
    }


    /**
     * 格式化日期(精确到日)
     */
    public static String formatDateForDay(Date date) {
        return daySdf.format(date);
    }

    /**
     * 格式化日期(精确到日)
     */
    public static String formatDateForDay(TemporalAccessor date) {
        return dayDtf.format(date);
    }

    /**
     * 格式化日期(精确到秒)
     */
    public static String formatDateForSecond(Date date) {
        return secondSdf.format(date);
    }

    /**
     * 格式化日期(精确到秒)
     */
    public static String formatDateForSecond(TemporalAccessor date) {
        return secondDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     */
    public static String formatDateForZhDay(Date date) {
        return zhDaySdf.format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     */
    public static String formatDateForZhDay(TemporalAccessor date) {
        return zhDayDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     */
    public static String formatDateForZhSecond(Date date) {
        return zhSecondSdf.format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     */
    public static String formatDateForZhSecond(TemporalAccessor date) {
        return zhSecondDtf.format(date);
    }

    /**
     * 格式化日期(无格式)
     */
    public static String formatDateForNoFormat(Date date) {
        return noFormatSdf.format(date);
    }

    /**
     * 格式化日期(无格式)
     */
    public static String formatDateForNoFormat(TemporalAccessor date) {
        return noFormatDtf.format(date);
    }

    /**
     * 将Date转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}