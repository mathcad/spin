package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期工具类
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public abstract class DateUtils {
    // 2017
    private static final String YEAR_PATTERN = "(\\d{4})";
    // 17
    private static final String SHORT_YEAR_PATTERN = "(\\d{2})";
    // 01-12 或 1-12
    private static final String MONTH_PATTERN = "(0?[1-9]|1[0-2])";
    // 01-31 或 1-31
    private static final String DAY_PATTERN = "(0?[1-9]|[1-2]\\d|3[0-1])";
    // 00-23 或 0-23
    private static final String HOUR_PATTERN = "(0?\\d|1\\d|2[0-3])";
    // 00-59 或 0-59
    private static final String MINUTE_PATTERN = "(0?\\d|[1-5]\\d)";
    // 00-59 或 0-59
    private static final String SECOND_PATTERN = MINUTE_PATTERN;
    // 123
    private static final String MILLION_SECOND_PATTERN = "(\\d{3})";

    private static final String ZONED_PATTERN_S = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[A-Za-z/]+";

    private static final String DAY = "yyyy-MM-dd";
    private static final String SECOND = "yyyy-MM-dd HH:mm:ss";
    private static final String MILLSEC = "yyyy-MM-dd HH:mm:ss SSS";
    private static final String ZH_DAY = "yyyy年MM月dd日";
    private static final String ZH_SECOND = "yyyy年MM月dd日 HH时mm分ss秒";
    private static final String FULL_DAY = "yyyy_MM_dd_HH_mm_ss_S";
    private static final String NO_FORMAT = "yyyyMMddHHmmss";
    private static final String ZONED_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSz";

    private static final ThreadLocal<SimpleDateFormat> daySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(DAY));
    private static final ThreadLocal<SimpleDateFormat> secondSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(SECOND));
    private static final ThreadLocal<SimpleDateFormat> millSecSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(MILLSEC));
    private static final ThreadLocal<SimpleDateFormat> zhDaySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(ZH_DAY));
    private static final ThreadLocal<SimpleDateFormat> zhSecondSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(ZH_SECOND));
    private static final ThreadLocal<SimpleDateFormat> fullDaySdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(FULL_DAY));
    private static final ThreadLocal<SimpleDateFormat> noFormatSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat(NO_FORMAT));

    private static final DateTimeFormatter dayDtf = DateTimeFormatter.ofPattern(DAY);
    private static final DateTimeFormatter secondDtf = DateTimeFormatter.ofPattern(SECOND);
    private static final DateTimeFormatter millSecDtf = DateTimeFormatter.ofPattern(MILLSEC);
    private static final DateTimeFormatter zhDayDtf = DateTimeFormatter.ofPattern(ZH_DAY);
    private static final DateTimeFormatter zhSecondDtf = DateTimeFormatter.ofPattern(ZH_SECOND);
    private static final DateTimeFormatter fullDayDtf = DateTimeFormatter.ofPattern(FULL_DAY);
    private static final DateTimeFormatter noFormatDtf = DateTimeFormatter.ofPattern(NO_FORMAT);
    private static final DateTimeFormatter zonedDtf = DateTimeFormatter.ofPattern(ZONED_FORMAT);


    private static final String[] datePatten = {
        YEAR_PATTERN + "(.)" + MONTH_PATTERN + "(.)" + DAY_PATTERN,
    };
    private static final String[] dateFormat = {
        "yyyy{0}MM{1}dd",
    };

    private static final String[] timePatten = {
        HOUR_PATTERN + ":" + MINUTE_PATTERN + ":" + SECOND_PATTERN + "\\." + MILLION_SECOND_PATTERN,
        HOUR_PATTERN + "时" + MINUTE_PATTERN + "分" + SECOND_PATTERN + "秒" + "\\." + MILLION_SECOND_PATTERN,
        HOUR_PATTERN + ":" + MINUTE_PATTERN + ":" + SECOND_PATTERN,
        HOUR_PATTERN + "时" + MINUTE_PATTERN + "分" + SECOND_PATTERN + "秒",
        HOUR_PATTERN + ":" + MINUTE_PATTERN,
        HOUR_PATTERN + "时" + MINUTE_PATTERN + "分",
        HOUR_PATTERN + "时",
        HOUR_PATTERN + "点"
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

    private static final Pattern ZONED_PATTERN = Pattern.compile(ZONED_PATTERN_S);

    static {
        for (int i = 0; i < datePatten.length; i++) {
            pattens[datePatten.length * timePatten.length + i] = Pattern.compile(datePatten[i]);
            for (int j = 0; j < timePatten.length; j++) {
                Pattern pattern = Pattern.compile(datePatten[i] + "([^0-9]+)" + timePatten[j]);
                pattens[i * (timePatten.length) + j] = pattern;
            }
        }
    }

    private DateUtils() {
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     *
     * @param date 日期字符串
     * @return 日期
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
            if (index < datePatten.length * timePatten.length) {
                sdf = new SimpleDateFormat(StringUtils.format(dateFormat[index / timeFormat.length], matcher.group(2), matcher.group(4))
                    + "'"
                    + matcher.group(6)
                    + "'"
                    + timeFormat[index % timeFormat.length]);
            } else {
                sdf = new SimpleDateFormat(StringUtils.format(dateFormat[index % (datePatten.length * timePatten.length)]
                    , matcher.group(2)
                    , matcher.group(4)));
            }
        }
        try {
            return sdf.parse(matcher == null ? date : matcher.group(0));
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "[" + date + "]");
        }
    }

    /**
     * 将日期字符串转换为日期
     *
     * @param date    日期字符串
     * @param pattern 日期格式
     * @return 日期
     */
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

    /**
     * 将java8新的时间日期转为Date
     *
     * @param date 时间日期
     * @return Date
     */
    public static Date toDate(TemporalAccessor date) {
        try {
            return null == date ? null : millSecSdf.get().parse(millSecDtf.format(date));
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, "时间转换失败", e);
        }
    }

    /**
     * 将日期字符串转换为日期(自动推断日期格式)
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static LocalDateTime toLocalDateTime(String date) {
        if (ZONED_PATTERN.matcher(date).matches()) {
            return ZonedDateTime.parse(date, zonedDtf).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        Date d = toDate(date);
        return toLocalDateTime(d);
    }

    /**
     * 将日期字符串转换为日期
     *
     * @param date    日期字符串
     * @param pattern 日期格式
     * @return 日期
     */
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
     *
     * @param date Date日期
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return null == date ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 将java.sql.Timestamp转换为LocalDateTime
     *
     * @param date java.sql.Timestamp日期
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Timestamp date) {
        return null == date ? null : date.toLocalDateTime();
    }

    /**
     * 将java.sql.Date转换为LocalDateTime
     *
     * @param date java.sql.Date日期
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(java.sql.Date date) {
        return null == date ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 增加毫秒
     *
     * @param date   日期
     * @param millis 毫秒数
     * @param <T>    日期类型参数
     * @return 结果时间
     */
    public static <T extends Temporal> T addMillis(T date, long millis) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(millis, ChronoUnit.MILLIS);
    }

    /**
     * 增加毫秒
     *
     * @param date   日期
     * @param millis 毫秒数
     * @return 结果时间
     */
    public static Date addMillis(Date date, int millis) {
        return null == date ? null : new Date(date.getTime() + millis);
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

    public static ZonedDateTime toZonedDateTime(String date) {
        return ZonedDateTime.parse(date, zonedDtf);
    }

    /**
     * 增加秒
     *
     * @param date    日期
     * @param seconds 秒数
     * @param <T>     日期类型参数
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
     * @param <T>     日期类型参数
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
     * @param <T>   日期类型参数
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
     * @param <T>  日期类型参数
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
     * @param <T>   日期类型参数
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
     * @param <T>    日期类型参数
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
     * @param <T>   日期类型参数
     * @return 结果时间
     */
    public static <T extends Temporal> T addYears(T date, int years) {
        //noinspection unchecked
        return null == date ? null : (T) date.plus(years, ChronoUnit.YEARS);
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForFullName(Date date) {
        return null == date ? null : fullDaySdf.get().format(date);
    }

    /**
     * 将日期格式化作为yyyy_MM_dd_HH_mm_ss_S
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForFullName(TemporalAccessor date) {
        return null == date ? null : fullDayDtf.format(date);
    }


    /**
     * 格式化日期(精确到日)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForDay(Date date) {
        return null == date ? null : daySdf.get().format(date);
    }

    /**
     * 格式化日期(精确到日)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForDay(TemporalAccessor date) {
        return null == date ? null : dayDtf.format(date);
    }

    /**
     * 格式化日期(精确到秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForSecond(Date date) {
        return null == date ? null : secondSdf.get().format(date);
    }

    /**
     * 格式化日期(精确到毫秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForMillSec(Date date) {
        return null == date ? null : millSecSdf.get().format(date);
    }

    /**
     * 格式化日期(精确到秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForSecond(TemporalAccessor date) {
        return null == date ? null : secondDtf.format(date);
    }

    /**
     * 格式化日期(精确到毫秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForMillSec(TemporalAccessor date) {
        return null == date ? null : millSecDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForZhDay(Date date) {
        return null == date ? null : zhDaySdf.get().format(date);
    }

    /**
     * 格式化中文日期(精确到日)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForZhDay(TemporalAccessor date) {
        return null == date ? null : zhDayDtf.format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForZhSecond(Date date) {
        return null == date ? null : zhSecondSdf.get().format(date);
    }

    /**
     * 格式化中文日期(精确到秒)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForZhSecond(TemporalAccessor date) {
        return null == date ? null : zhSecondDtf.format(date);
    }

    /**
     * 格式化日期(无格式)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForNoFormat(Date date) {
        return null == date ? null : noFormatSdf.get().format(date);
    }

    /**
     * 格式化日期(无格式)
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateForNoFormat(TemporalAccessor date) {
        return null == date ? null : noFormatDtf.format(date);
    }

    public static String formatDateForZoned(ZonedDateTime zonedDateTime) {
        return zonedDtf.format(zonedDateTime);
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String pattern) {
        if (StringUtils.isEmpty(pattern))
            return null == date ? null : secondSdf.get().format(date);
        return null == date ? null : new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
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
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 604800000;
                case 'd':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 86400000;
                case 'h':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 3600000;
                case 'm':
                    return Long.parseLong(period.substring(0, period.length() - 1)) * 60000;
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
     * @return 是否过期
     */
    public static boolean isTimeOut(Long time, Long expiredIn) {
        return (System.currentTimeMillis() - time) > expiredIn;
    }

    /**
     * 判断是否过期
     *
     * @param time      待判断时间
     * @param expiredIn 有效期
     * @return 是否过期
     */
    public static boolean isTimeOut(Date time, Long expiredIn) {
        return (System.currentTimeMillis() - time.getTime()) > expiredIn;
    }

    /**
     * 判断是否过期
     *
     * @param time      待判断时间
     * @param expiredIn 有效期
     * @return 是否过期
     */
    public static boolean isTimeOut(LocalDateTime time, Long expiredIn) {
        return LocalDateTime.now().isAfter(time.plus(expiredIn, ChronoUnit.MILLIS));
    }

    /**
     * 判断value1与value2形成的时间段是否与value3与value4形成的时间段相交
     * <p>时间段的端点不需要保证有序，所有端点都不允许为空</p>
     *
     * @param value1 时间段1的端点1
     * @param value2 时间段1的端点2
     * @param value3 时间段2的端点1
     * @param value4 时间段2的端点2
     * @return 是否相交
     */
    public static boolean interact(Date value1, Date value2, Date value3, Date value4) {
        return NumericUtils.interact(value1.getTime(), value2.getTime(), value3.getTime(), value4.getTime());
    }

    public static String prettyDuration(Date time) {
        return prettyDuration(time, new Date());
    }

    public static String prettyDuration(TemporalAccessor time) {
        return prettyDuration(DateUtils.toDate(time), new Date());
    }

    public static String prettyDuration(Date start, Date end) {
        Assert.notNull(start);
        Assert.notNull(end);
        long duration = end.getTime() - start.getTime();
        return prettyDuration(duration);
    }

    public static String prettyDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        boolean backward = seconds < 0;
        String suffix = backward ? "后" : "前";
        seconds = Math.abs(seconds);
        if (seconds < 5) {
            return backward ? "即将" : "刚刚";
        }
        if (seconds < 60) {
            return seconds + "秒" + suffix;
        }
        seconds /= 60;
        if (seconds < 29) {
            return seconds + "分钟" + suffix;
        }
        if (seconds < 31) {
            return "半小时" + suffix;
        }
        if (seconds < 60) {
            return seconds + "分钟" + suffix;
        }
        long m = seconds % 60;
        seconds /= 60;
        if (seconds < 24) {
            return (28 < m && m < 32 && seconds < 10 ? seconds + "个半小时" : seconds + "小时" + m + "分钟") + suffix;
        }
        seconds /= 24;
        if (seconds < 30) {
            if (seconds % 7 == 0) {
                return seconds / 7 + "周" + suffix;
            }
            return seconds + "天" + suffix;
        }
        if (seconds < 365) {
            return seconds / 30 + "月" + suffix;
        }
        return seconds / 365 + "年" + suffix;
    }

    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameDay(final TemporalAccessor date1, final TemporalAccessor date2) {
        return null != date1 && null != date2 && date1.get(ChronoField.YEAR) == date2.get(ChronoField.YEAR)
            && date1.get(ChronoField.DAY_OF_YEAR) == date2.get(ChronoField.DAY_OF_YEAR);
    }

    public static boolean isSameDay(final TemporalAccessor date1, final Date date2) {
        LocalDateTime d2 = toLocalDateTime(date2);
        return null != date1 && null != d2 && date1.get(ChronoField.YEAR) == d2.get(ChronoField.YEAR)
            && date1.get(ChronoField.DAY_OF_YEAR) == d2.get(ChronoField.DAY_OF_YEAR);
    }
}
