package org.spin.core.util;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
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
    // 2017
    private static final String yearPattern = "(\\d{4})";
    // 17
    private static final String shortYearPattern = "(\\d{2})";
    // 01-12 或 1-12
    private static final String monthPattern = "(0?[1-9]|1[0-2])";
    // 01-31 或 1-31
    private static final String dayPattern = "(0?[1-9]|[1-2]\\d|3[0-1])";
    // 00-23 或 0-23
    private static final String hourPattern = "(0?\\d|1\\d|2[0-3])";
    // 00-59 或 0-59
    private static final String minutePattern = "(0?\\d|[1-5]\\d)";
    // 00-59 或 0-59
    private static final String secondPattern = minutePattern;
    // 123
    private static final String millionSecondPattern = "(\\d{3})";

    private static final String day = "yyyy-MM-dd";
    private static final String second = "yyyy-MM-dd HH:mm:ss";
    private static final String millSec = "yyyy-MM-dd HH:mm:ss SSS";
    private static final String zhDay = "yyyy年MM月dd日";
    private static final String zhSecond = "yyyy年MM月dd日 HH时mm分ss秒";
    private static final String fullDay = "yyyy_MM_dd_HH_mm_ss_S";
    private static final String noFormat = "yyyyMMddHHmmss";

    private static final ThreadLocal<SimpleDateFormat> daySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(day));
    private static final ThreadLocal<SimpleDateFormat> secondSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(second));
    private static final ThreadLocal<SimpleDateFormat> millSecSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(millSec));
    private static final ThreadLocal<SimpleDateFormat> zhDaySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(zhDay));
    private static final ThreadLocal<SimpleDateFormat> zhSecondSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(zhSecond));
    private static final ThreadLocal<SimpleDateFormat> fullDaySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(fullDay));
    private static final ThreadLocal<SimpleDateFormat> noFormatSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(noFormat));

    private static final DateTimeFormatter dayDtf = DateTimeFormatter.ofPattern(day);
    private static final DateTimeFormatter secondDtf = DateTimeFormatter.ofPattern(second);
    private static final DateTimeFormatter millSecDtf = DateTimeFormatter.ofPattern(millSec);
    private static final DateTimeFormatter zhDayDtf = DateTimeFormatter.ofPattern(zhDay);
    private static final DateTimeFormatter zhSecondDtf = DateTimeFormatter.ofPattern(zhSecond);
    private static final DateTimeFormatter fullDayDtf = DateTimeFormatter.ofPattern(fullDay);
    private static final DateTimeFormatter noFormatDtf = DateTimeFormatter.ofPattern(noFormat);


    private static final String[] datePatten = {
        yearPattern + "(.)" + monthPattern + "(.)" + dayPattern,
    };
    private static final String[] dateFormat = {
        "yyyy{0}MM{1}dd",
    };

    private static final String[] timePatten = {
        hourPattern + ":" + minutePattern + ":" + secondPattern + "\\." + millionSecondPattern,
        hourPattern + "时" + minutePattern + "分" + secondPattern + "秒" + "\\." + millionSecondPattern,
        hourPattern + ":" + minutePattern + ":" + secondPattern,
        hourPattern + "时" + minutePattern + "分" + secondPattern + "秒",
        hourPattern + ":" + minutePattern,
        hourPattern + "时" + minutePattern + "分",
        hourPattern + "时",
        hourPattern + "点"
    };

    private static final String[] timeFormat = {
        "HH:mm:ss.SSS",
        "HH时mm分ss.SSS",
        "HH:mm:ss",
        "HH时mm分ss",
        "HH:mm",
        "HH时mm分",
        "HH时",
        "HH点"
    };

    private static final Pattern[] pattens = new Pattern[datePatten.length * timePatten.length + datePatten.length];

    static {
        for (int i = 0; i < datePatten.length; i++) {
            pattens[datePatten.length * timePatten.length + i] = Pattern.compile(datePatten[i]);
            for (int j = 0; j < timePatten.length; j++) {
                Pattern pattern = Pattern.compile(datePatten[i] + "([^0-9]+)" + timePatten[j]);
                pattens[i * (timePatten.length) + j] = pattern;
            }
        }
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     */
    public static Date toDate(String date) {
        if (StringUtils.isEmpty(date))
            return null;
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
                    , matcher.group(4)) + "'" + matcher.group(6) + "'" + timeFormat[index % timeFormat.length]);
            else
                sdf = new SimpleDateFormat(dateFormat[index % (datePatten.length * timePatten.length)]);
        }
        try {
            return sdf.parse(matcher == null ? date : matcher.group(0));
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    public static Date toDate(String date, String pattern) {
        if (StringUtils.isEmpty(date))
            return null;
        SimpleDateFormat sdf;
        if (StringUtils.isEmpty(pattern))
            sdf = secondSdf.get();
        else
            sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    public static Date toDate(TemporalAccessor date) {
        try {
            return null == date ? null : millSecSdf.get().parse(millSecDtf.format(date));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     */
    public static LocalDateTime toLocalDateTime(String date) {
        if (StringUtils.isEmpty(date))
            return null;
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
            formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        else {
            if (index < datePatten.length * timePatten.length)
                formatter = DateTimeFormatter.ofPattern(StringUtils.format(dateFormat[index / timeFormat.length], matcher.group(2)
                    , matcher.group(4)) + "'" + matcher.group(6) + "'" + timeFormat[index % timeFormat.length]);
            else
                formatter = DateTimeFormatter.ofPattern(dateFormat[index % (datePatten.length * timePatten.length)]);
        }
        try {
            return LocalDateTime.parse(matcher == null ? date : matcher.group(0), formatter);
        } catch (DateTimeParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    public static LocalDateTime toLocalDateTime(String date, String pattern) {
        if (StringUtils.isEmpty(date))
            return null;
        DateTimeFormatter dtf;
        if (StringUtils.isEmpty(pattern))
            dtf = secondDtf;
        else
            dtf = DateTimeFormatter.ofPattern(pattern);
        try {
            return LocalDateTime.parse(date, dtf);
        } catch (DateTimeParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    /**
     * 将Date转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return null == date ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 将java.sql.Timestamp转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Timestamp date) {
        return null == date ? null : date.toLocalDateTime();
    }

    /**
     * 将java.sql.Date转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(java.sql.Date date) {
        return null == date ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 增加秒
     *
     * @param date    日期
     * @param seconds 秒数
     * @return 结果时间
     */
    public static Date addSeconds(Date date, int seconds) {
        return null == date ? null : new Date(date.getTime() + seconds * 1000L);
    }

    /**
     * 增加秒
     *
     * @param date    日期
     * @param seconds 秒数
     * @return 结果时间
     */
    public static <T extends Temporal> T addSeconds(T date, int seconds) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * 增加分
     *
     * @param date    日期
     * @param minutes 分钟数
     * @return 结果时间
     */
    public static Date addMinutes(Date date, int minutes) {
        return null == date ? null : new Date(date.getTime() + minutes * 60000L);
    }

    /**
     * 增加分
     *
     * @param date    日期
     * @param minutes 分钟数
     * @return 结果时间
     */
    public static <T extends Temporal> T addMinutes(T date, int minutes) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * 增加小时
     *
     * @param date  日期
     * @param hours 小时数
     * @return 结果时间
     */
    public static Date addHours(Date date, int hours) {
        return null == date ? null : new Date(date.getTime() + hours * 3600000L);
    }

    /**
     * 增加小时
     *
     * @param date  日期
     * @param hours 小时数
     * @return 结果时间
     */
    public static <T extends Temporal> T addHours(T date, int hours) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(hours, ChronoUnit.HOURS);
    }

    /**
     * 增加天
     *
     * @param date 日期
     * @param days 分钟数
     * @return 结果时间
     */
    public static Date addDays(Date date, int days) {
        return null == date ? null : new Date(date.getTime() + days * 86400000L);
    }

    /**
     * 增加天
     *
     * @param date 日期
     * @param days 分钟数
     * @return 结果时间
     */
    public static <T extends Temporal> T addDays(T date, int days) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(days, ChronoUnit.DAYS);
    }

    /**
     * 增加周
     *
     * @param date  日期
     * @param weeks 周数
     * @return 结果时间
     */
    public static Date addWeeks(Date date, int weeks) {
        return null == date ? null : new Date(date.getTime() + weeks * 604800000L);
    }

    /**
     * 增加周
     *
     * @param date  日期
     * @param weeks 周数
     * @return 结果时间
     */
    public static <T extends Temporal> T addWeeks(T date, int weeks) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(weeks, ChronoUnit.WEEKS);
    }

    /**
     * 增加月
     *
     * @param date   日期
     * @param months 月数
     * @return 结果时间
     */
    public static Date addMonths(Date date, int months) {
        if (null == date)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 增加月
     *
     * @param date   日期
     * @param months 月数
     * @return 结果时间
     */
    public static <T extends Temporal> T addMonths(T date, int months) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(months, ChronoUnit.MONTHS);
    }

    /**
     * 增加年
     *
     * @param date  日期
     * @param years 年数
     * @return 结果时间
     */
    public static Date addYears(Date date, int years) {
        if (null == date)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * 增加年
     *
     * @param date  日期
     * @param years 年数
     * @return 结果时间
     */
    public static <T extends Temporal> T addYears(T date, int years) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(years, ChronoUnit.YEARS);
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     */
    public static String formatDateForFullName(Date date) {
        return null == date ? null : fullDaySdf.get().format(date);
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     */
    public static String formatDateForFullName(TemporalAccessor date) {
        return null == date ? null : fullDayDtf.format(date);
    }


    /**
     * 格式化日期(精确到日)
     */
    public static String formatDateForDay(Date date) {
        return null == date ? null : daySdf.get().format(date);
    }

    /**
     * 格式化日期(精确到日)
     */
    public static String formatDateForDay(TemporalAccessor date) {
        return null == date ? null : dayDtf.format(date);
    }

    /**
     * 格式化日期(精确到秒)
     */
    public static String formatDateForSecond(Date date) {
        return null == date ? null : secondSdf.get().format(date);
    }

    /**
     * 格式化日期(精确到毫秒)
     */
    public static String formatDateForMillSec(Date date) {
        return null == date ? null : millSecSdf.get().format(date);
    }

    /**
     * 格式化日期(精确到秒)
     */
    public static String formatDateForSecond(TemporalAccessor date) {
        return null == date ? null : secondDtf.format(date);
    }

    /**
     * 格式化日期(精确到毫秒)
     */
    public static String formatDateForMillSec(TemporalAccessor date) {
        return null == date ? null : millSecDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     */
    public static String formatDateForZhDay(Date date) {
        return null == date ? null : zhDaySdf.get().format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     */
    public static String formatDateForZhDay(TemporalAccessor date) {
        return null == date ? null : zhDayDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     */
    public static String formatDateForZhSecond(Date date) {
        return null == date ? null : zhSecondSdf.get().format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     */
    public static String formatDateForZhSecond(TemporalAccessor date) {
        return null == date ? null : zhSecondDtf.format(date);
    }

    /**
     * 格式化日期(无格式)
     */
    public static String formatDateForNoFormat(Date date) {
        return null == date ? null : noFormatSdf.get().format(date);
    }

    /**
     * 格式化日期(无格式)
     */
    public static String formatDateForNoFormat(TemporalAccessor date) {
        return null == date ? null : noFormatDtf.format(date);
    }

    /**
     * 格式化日期
     */
    public static String format(Date date, String pattern) {
        if (StringUtils.isEmpty(pattern))
            return null == date ? null : secondSdf.get().format(date);
        return null == date ? null : new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 格式化日期
     */
    public static String format(TemporalAccessor date, String pattern) {
        if (StringUtils.isEmpty(pattern))
            return null == date ? null : secondDtf.format(date);
        return null == date ? null : DateTimeFormatter.ofPattern(pattern).format(date);
    }

    /**
     * 将时间段解析为毫秒最后一位区分单位
     * <pre>
     *     例：1d = 86,400,000
     *     如果没有单位，相当于{@code Long.parseLong(period)}
     *     w:周
     *     d:天
     *     h:时
     *     m:分
     *     s:秒
     * </pre>
     *
     * @param period 时间, 格式必须符合：{@code \d+[wdhms]?}，如15d
     * @return 对应的毫秒数
     */
    public static Long periodToMs(String period) {
        try {

            switch (period.charAt(period.length() - 1)) {
                case 'w':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 1000 * 60 * 60 * 24 * 7;
                case 'd':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 1000 * 60 * 60 * 24;
                case 'h':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 1000 * 60 * 60;
                case 'm':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 1000 * 60;
                case 's':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 1000;
                default:
                    return Long.parseLong(period);
            }
        } catch (Exception ignore) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "时间段字符串格式不正确");
        }
    }

    /**
     * 判断是否过期
     *
     * @param time      待判断时间
     * @param expiredIn 有效期
     */
    public static boolean isTimeOut(Long time, Long expiredIn) {
        return (System.currentTimeMillis() - time) > expiredIn;
    }

    /**
     * 判断是否过期
     *
     * @param time      待判断时间
     * @param expiredIn 有效期
     */
    public static boolean isTimeOut(Date time, Long expiredIn) {
        return (System.currentTimeMillis() - time.getTime()) > expiredIn;
    }

    /**
     * 判断是否过期
     *
     * @param time      待判断时间
     * @param expiredIn 有效期
     */
    public static boolean isTimeOut(LocalDateTime time, Long expiredIn) {
        return LocalDateTime.now().compareTo(time.plus(expiredIn, ChronoUnit.MILLIS)) > 0;
    }
}
