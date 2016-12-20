package org.infrastructure.util;

import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.throwable.SimplifiedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 日期工具类
 *
 * @author xuweinan
 */
public class DateUtils {
    private static final String yearPattern = "(\\d{4})";
    private static final String shortYearPattern = "(\\d{2})";
    private static final String alignMonthPattern = "(0[1-9]|1[0-2])";
    private static final String alignDayPattern = "(0[1-9]|[1-2]\\d|3[0-1])";
    private static final String alignHourPattern = "([0-1]\\d|2[0-3])";
    private static final String alignMinutePattern = "([0-5]\\d)";
    private static final String alignSecondPattern = "([0-5]\\d)";

    private static SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat second = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat zhDay = new SimpleDateFormat("yyyy年MM月dd日");
    private static SimpleDateFormat zhSecond = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    private static SimpleDateFormat fullDay = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S");

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

    public static Date parseDate(String date) {
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
     * 计算提前多少秒时间
     *
     * @param date   日期
     * @param second 秒数
     * @return 结果时间
     */
    public static Date beforeSecond(Date date, int second) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) - second);

        return calendar.getTime();

    }

    /**
     * 计算延后多少小时时间
     *
     * @param date  日期
     * @param hours 小时数
     * @return 结果时间
     */
    public static Date afterhours(Date date, int hours) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + hours);

        return calendar.getTime();
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     */
    public static String formatDateForFullName(Date date) {
        return fullDay.format(date);
    }


    /**
     * 格式化日期(精确到日)
     */
    public static String formatDateForDay(Date date) {
        return day.format(date);
    }

    /**
     * 格式化日期(精确到秒)
     */
    public static String formatDateForSecond(Date date) {
        return second.format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     */
    public static String formatDateForZhDay(Date date) {
        return zhDay.format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     */
    public static String formatDateForZhSecond(Date date) {
        return zhSecond.format(date);
    }

}
