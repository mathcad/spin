package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Created by xuweinan on 2017/11/23.</p>
 *
 * @author xuweinan
 */
public final class NumericUtils extends Util {
    private static final String UNKNOWN_NUM = "无法识别的数字格式";
    private static final Pattern numPattern = Pattern.compile("^([-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]+)[^-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]{0,2}$");
    private static final Pattern pureNum = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final String[] scales = {"亿", "万", "千", "仟", "百", "佰", "十", "拾"};
    private static final Map<String, Long> numMap = new HashMap<>();

    static {
        numMap.put("零", 0L);
        numMap.put("〇", 0L);
        numMap.put("一", 1L);
        numMap.put("壹", 1L);
        numMap.put("二", 2L);
        numMap.put("贰", 2L);
        numMap.put("两", 2L);
        numMap.put("俩", 2L);
        numMap.put("三", 3L);
        numMap.put("叁", 3L);
        numMap.put("仨", 3L);
        numMap.put("四", 4L);
        numMap.put("肆", 4L);
        numMap.put("五", 5L);
        numMap.put("伍", 5L);
        numMap.put("六", 6L);
        numMap.put("陆", 6L);
        numMap.put("七", 7L);
        numMap.put("柒", 7L);
        numMap.put("八", 8L);
        numMap.put("捌", 8L);
        numMap.put("九", 9L);
        numMap.put("玖", 9L);
        numMap.put("0", 0L);
        numMap.put("1", 1L);
        numMap.put("2", 2L);
        numMap.put("3", 3L);
        numMap.put("4", 4L);
        numMap.put("5", 5L);
        numMap.put("6", 6L);
        numMap.put("7", 7L);
        numMap.put("8", 8L);
        numMap.put("9", 9L);
    }

    private NumericUtils() {
    }

    public static byte toByte(Object value, int... defaultValue) {
        if (value instanceof Byte) {
            return (byte) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return (byte) defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return (byte) defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    public static short toShort(Object value, int... defaultValue) {
        if (value instanceof Short) {
            return (short) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return (short) defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return (short) defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    public static int toInt(Object value, int... defaultValue) {
        if (value instanceof Integer) {
            return (int) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    public static float toFloat(Object value, float... defaultValue) {
        if (value instanceof Float) {
            return (float) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    public static long toLong(Object value, long... defaultValue) {
        if (value instanceof Long) {
            return (long) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    public static double toDouble(Object value, double... defaultValue) {
        if (value instanceof Double) {
            return (double) value;
        }

        if (null == value) {
            if (defaultValue != null && defaultValue.length > 0) {
                return defaultValue[0];
            } else {
                throw new NumberFormatException("null不是合法的数字");
            }
        }

        if (value instanceof CharSequence && isNum(value)) {
            value = toBigDecimal(value);
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (defaultValue != null && defaultValue.length > 0) {
            return defaultValue[0];
        } else {
            throw new NumberFormatException(value.toString() + "不是合法的数字");
        }
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Byte nullToZero(Byte value) {
        return null == value ? 0 : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Short nullToZero(Short value) {
        return null == value ? 0 : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Integer nullToZero(Integer value) {
        return null == value ? 0 : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Long nullToZero(Long value) {
        return null == value ? 0L : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Float nullToZero(Float value) {
        return null == value ? 0F : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static Double nullToZero(Double value) {
        return null == value ? 0D : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static BigDecimal nullToZero(BigDecimal value) {
        return null == value ? BigDecimal.ZERO : value;
    }

    /**
     * 如果value是null，返回0，否则返回原始值
     *
     * @param value 原始值
     * @return 0或原始值
     */
    public static BigInteger nullToZero(BigInteger value) {
        return null == value ? BigInteger.ZERO : value;
    }

    /**
     * 将任意数字转换为对应精度的BigDecimal,如果为null，则返回BigDecimal.ZERO
     *
     * @param value 原始值
     * @return 转换后的BigDecimal数值
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (null == value) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof CharSequence || value instanceof Number) {
            try {
                return new BigDecimal(value.toString());
            } catch (Exception e) {
                throw new SpinException(ErrorCode.INVALID_PARAM, "参数不是合法的数值", e);
            }
        } else {
            throw new SpinException(ErrorCode.INVALID_PARAM, "参数不是合法的数值");
        }
    }

    /**
     * 将任意数字转换为指定精度的BigDecimal
     *
     * @param value        原始值
     * @param defaultValue value为null时的默认值
     * @param scale        精度(可以为null)
     * @return 转换后的BigDecimal数值
     */
    public static BigDecimal toBigDecimal(Number value, BigDecimal defaultValue, Integer scale) {
        if (null == value) {
            return defaultValue;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        BigDecimal bigDecimal = new BigDecimal(value.toString());
        if (null != scale) {
            bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        }
        return bigDecimal;
    }

    public static BigDecimal up(Object value, int scale) {
        return scale(value, scale, RoundingMode.UP);
    }

    public static BigDecimal down(Object value, int scale) {
        return scale(value, scale, RoundingMode.DOWN);
    }

    public static BigDecimal ceiling(Object value, int scale) {
        return scale(value, scale, RoundingMode.CEILING);
    }

    public static BigDecimal floor(Object value, int scale) {
        return scale(value, scale, RoundingMode.FLOOR);
    }

    public static BigDecimal halfUp(Object value, int scale) {
        return scale(value, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal halfDown(Object value, int scale) {
        return scale(value, scale, RoundingMode.HALF_DOWN);
    }

    public static BigDecimal halfEven(Object value, int scale) {
        return scale(value, scale, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal scale(Object value, int scale, RoundingMode roundingMode) {
        if (null == value) {
            return BigDecimal.ZERO.setScale(scale, roundingMode);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(scale, roundingMode);
        }

        if (value instanceof CharSequence || value instanceof Number) {
            try {
                return new BigDecimal(value.toString()).setScale(scale, roundingMode);
            } catch (Exception e) {
                throw new SpinException(ErrorCode.INVALID_PARAM, "参数不是合法的数值", e);
            }
        } else {
            throw new SpinException(ErrorCode.INVALID_PARAM, "参数不是合法的数值");
        }
    }

    /**
     * 比较两个数值的大小(NullSafe)
     * <p>为null的参数，视为负无穷大，即比任何数都小</p>
     *
     * @param value1 数值1
     * @param value2 数值2
     * @return 数值1与数值2相等返回0，数值1大于数值2返回1，数值1小于数值2返回-1
     */
    public static int valueCompare(Number value1, Number value2) {
        if (null == value1 && null == value2) {
            return 0;
        } else if (null == value1) {
            return -1;
        } else if (null == value2) {
            return 1;
        } else {
            double diff = value1.doubleValue() - value2.doubleValue();
            return diff > 0 ? 1 : (diff == 0 ? 0 : -1);
        }
    }

    /**
     * 判断interval_1_1与interval_1_2形成的闭区间是否与interval_2_1与interval_2_2形成的闭区间相交
     * <p>区间的端点不需要保证有序，所有端点都不允许为空</p>
     *
     * @param interval_1_1 区间1的端点1
     * @param interval_1_2 区间1的端点2
     * @param interval_2_1 区间2的端点1
     * @param interval_2_2 区间2的端点2
     * @param <T> 类型
     * @return 是否相交
     */
    public static <T extends Comparable<T>> boolean interact(T interval_1_1, T interval_1_2, T interval_2_1, T interval_2_2) {
        T tmp;

        if (Assert.notNull(interval_1_1, "interval_1_1不能为空").compareTo(Assert.notNull(interval_1_2, "interval_1_2不能为空")) > 0) {
            tmp = interval_1_1;
            interval_1_1 = interval_1_2;
            interval_1_2 = tmp;
        }

        if (Assert.notNull(interval_2_1, "interval_2_1不能为空").compareTo(Assert.notNull(interval_2_2, "interval_2_2不能为空")) > 0) {
            tmp = interval_2_1;
            interval_2_1 = interval_2_2;
            interval_2_2 = tmp;
        }

        return !(interval_2_1.compareTo(interval_1_2) > 0 || interval_2_2.compareTo(interval_1_1) < 0);
    }

    /**
     * 分析数字，将中文数字转换为阿拉伯数字
     * 兼容中文与数字混合，只要数字语义正确即可
     *
     * @param str 数字字符串
     * @return 阿拉伯数字的字符串形式
     */
    public static String analysisNumber(String str) {
        Matcher matcher = numPattern.matcher(str.trim());
        if (!matcher.matches()) {
            throw new SpinException("无法识别的数字格式，数字不能包括“负-0123456789零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.”以外的字符");
        }
        String tmp = matcher.group(1);
        // 本身是合法数字直接return
        if (pureNum.matcher(tmp).matches()) {
            if (tmp.charAt(tmp.length() - 1) == '.') {
                throw new SpinException("无法识别的数字格式，数字不能以.结尾");
            }
            return tmp;
        }

        // 识别符号
        String sign = "";
        if (tmp.charAt(0) == '负' || tmp.charAt(0) == '-') {
            sign = "-";
            tmp = tmp.substring(1);
            for (char c : tmp.toCharArray()) {
                if (c == '负' || c == '-') {
                    throw new SpinException("无法识别的数字格式，符号位只能出现在数字起始部位");
                }
            }
        }

        // 分割整数部分与小数部分
        String splitter = tmp.contains("点") ? "点" : "\\.";
        String[] parts = tmp.split(splitter);
        String n = parts[0];
        String d = parts.length > 1 ? parts[1] : null;

        // 整数部分
        Long resN = 0L;
        parts = n.split("零");
        for (String p : parts) {
            resN += decodeNum(p);
        }
        StringBuilder resD = null;

        // 小数部分
        if (d != null) {
            resD = new StringBuilder();
            for (int i = 0; i != d.length(); ++i) {
                if (!numMap.containsKey(String.valueOf(d.charAt(i)))) {
                    throw new SpinException("无法识别的数字格式，小数部分不正确");
                }
                resD.append(numMap.get(String.valueOf(d.charAt(i))));
            }
        }

        return resD == null ? sign + resN.toString() : sign + resN.toString() + "." + resD.toString();
    }

    /**
     * 判断一个对象是否是一个数字或数字字符串
     *
     * @param object 待判断对象
     * @return 是否是一个数字，或者数字字符串
     */
    public static boolean isNum(Object object) {
        if (object instanceof Number) {
            return true;
        } else if (object instanceof CharSequence) {
            return StringUtils.isNumeric(object.toString());
        } else {
            return false;
        }
    }

    public static short compositeShort(byte[] bytes, int from) {
        return compositeShort(bytes, from, 2);
    }

    public static short compositeShort(byte[] bytes, int from, int len) {
        Assert.inclusiveBetween(1, 2, len, "短整型的长度必须在1-2之间");
        return (short) compositeInt(bytes, from, len);
    }

    public static int compositeInt(byte[] bytes, int from) {
        return compositeInt(bytes, from, 4);
    }

    public static int compositeInt(byte[] bytes, int from, int len) {
        Assert.inclusiveBetween(0, bytes.length, from, "from索引超出范围: " + 0 + " - " + bytes.length);
        Assert.inclusiveBetween(1, 4, len, "整型的长度必须在1-4之间");
        Assert.isTrue(bytes.length - from >= len, "数据不合法");
        int mask = (len - 1) << 3;
        int res = 0;
        while (--len != -1) {
            res = res | (bytes[from + len] << (mask - len * 8));
        }
        return res;
    }

    public static long compositeLong(byte[] bytes, int from) {
        return compositeLong(bytes, from, 8);
    }

    public static long compositeLong(byte[] bytes, int from, int len) {
        Assert.inclusiveBetween(0, bytes.length, from, "from索引超出范围: " + 0 + " - " + bytes.length);
        Assert.inclusiveBetween(1, 8, len, "长整型的长度必须在1-8之间");
        Assert.isTrue(bytes.length - from >= len, "数据不合法");
        int mask = (len - 1) << 3;
        long res = 0;
        while (--len != -1) {
            res = res | (bytes[from + len] << (mask - len * 8));
        }
        return res;
    }


    private static Long decodeNum(String n) {
        if (pureNum.matcher(n).matches())
            return Long.parseLong(n);
        String tmp = n;
        String[] parts;
        Long resN = 0L;
        while (tmp != null) {
            String scale = lookupScale(tmp);
            Long h;
            switch (scale) {
                case "亿":
                    parts = tmp.split(scale);
                    if (parts.length > 2) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    h = decodeNum(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 100000000L;
                    break;
                case "万":
                    parts = tmp.split(scale);
                    if (parts.length > 2) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    h = decodeNum(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 10000L;
                    break;
                case "千":
                case "仟":
                    parts = tmp.split(scale);
                    if (parts.length > 2) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    h = parts[0].length() > 1 ? Long.parseLong(parts[0]) : numMap.get(parts[0]);

                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 1000L;
                    break;
                case "百":
                case "佰":
                    parts = tmp.split(scale);
                    if (parts.length > 2) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    h = parts[0].length() > 1L ? Long.parseLong(parts[0]) : numMap.get(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 100L;
                    break;
                case "十":
                case "拾":
                    parts = tmp.split(scale);
                    if (parts.length > 2) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    h = tmp.startsWith("十") ? 1L : (parts[0].length() > 1 ? Long.parseLong(parts[0]) : numMap.get(parts[0]));
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 10L;
                    break;
                default:
                    if (!numMap.containsKey(tmp)) {
                        throw new SpinException(UNKNOWN_NUM);
                    }
                    resN += numMap.get(tmp);
                    tmp = null;
            }
        }
        return resN;
    }

    private static String lookupScale(String numU) {
        for (String s : scales) {
            if (numU.contains(s)) {
                return s;
            }
        }
        return "";
    }

}
