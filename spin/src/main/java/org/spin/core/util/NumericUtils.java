package org.spin.core.util;

import org.spin.core.throwable.SimplifiedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Created by xuweinan on 2017/11/23.</p>
 *
 * @author xuweinan
 */
public abstract class NumericUtils {
    private static Pattern numPattern = Pattern.compile("^([-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]+)[^-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]{0,2}$");
    private static Pattern pureNum = Pattern.compile("^-?\\d+$|^\\d+\\.\\d+$");
    private static String[] scales = {"亿", "万", "千", "仟", "百", "佰", "十", "拾"};
    private static Map<String, Long> numMap = new HashMap<>();

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
     * 比较两个数值的大小(NullSafe)
     * <p>为null的参数，视为负无穷大，即比任何数都小</p>
     *
     * @param value1 数值1
     * @param value2 数值2
     * @return 数值1与数值2相等返回0，数值1大于数值2返回1，数值1小与数值2返回-1
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
     * 判断value1与value2形成的闭区间是否与value3与value4形成的闭区间相交
     * <p>区间的端点不需要保证有序，所有端点都不允许为空</p>
     *
     * @param value1 区间1的端点1
     * @param value2 区间1的端点2
     * @param value3 区间2的端点1
     * @param value4 区间2的端点2
     * @return 是否相交
     */
    public static boolean interact(Number value1, Number value2, Number value3, Number value4) {
        double v1 = value1.doubleValue();
        double v2 = value2.doubleValue();
        double v3 = value3.doubleValue();
        double v4 = value4.doubleValue();
        return !(v1 < v3 && v1 < v4 && v2 < v3 && v2 < v4 || v3 < v1 && v3 < v2 && v4 < v1 && v4 < v2);
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
        if (!matcher.matches())
            throw new SimplifiedException("无法识别的数字格式，个数字不能包括“负-0123456789零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.”以外的字符");
        String tmp = matcher.group(1);
        // 本身是合法数字直接return
        if (pureNum.matcher(tmp).matches()) {
            if (tmp.charAt(tmp.length() - 1) == '.')
                throw new SimplifiedException("无法识别的数字格式，数字不能以.结尾");
            return tmp;
        }

        // 识别符号
        String sign = "";
        if (tmp.charAt(0) == '负' || tmp.charAt(0) == '-') {
            sign = "-";
            tmp = tmp.substring(1);
            for (char c : tmp.toCharArray()) {
                if (c == '负' || c == '-')
                    throw new SimplifiedException("无法识别的数字格式，符号位只能出现在数字起始部位");
            }
        }

        // 分割整数部分与小数部分
        String spliter = tmp.contains("点") ? "点" : "\\.";
        String[] parts = tmp.split(spliter);
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
                if (!numMap.containsKey(String.valueOf(d.charAt(i))))
                    throw new SimplifiedException("无法识别的数字格式，小数部分不正确");
                resD.append(numMap.get(String.valueOf(d.charAt(i))));
            }
        }

        return resD == null ? sign + resN.toString() : sign + resN.toString() + "." + resD.toString();
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
                    if (parts.length > 2)
                        throw new SimplifiedException("无法识别的数字格式");
                    h = decodeNum(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 100000000L;
                    break;
                case "万":
                    parts = tmp.split(scale);
                    if (parts.length > 2)
                        throw new SimplifiedException("无法识别的数字格式");
                    h = decodeNum(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 10000L;
                    break;
                case "千":
                case "仟":
                    parts = tmp.split(scale);
                    if (parts.length > 2)
                        throw new SimplifiedException("无法识别的数字格式");
                    h = parts[0].length() > 1 ? Long.parseLong(parts[0]) : numMap.get(parts[0]);

                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 1000L;
                    break;
                case "百":
                case "佰":
                    parts = tmp.split(scale);
                    if (parts.length > 2)
                        throw new SimplifiedException("无法识别的数字格式");
                    h = parts[0].length() > 1L ? Long.parseLong(parts[0]) : numMap.get(parts[0]);
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 100L;
                    break;
                case "十":
                case "拾":
                    parts = tmp.split(scale);
                    if (parts.length > 2)
                        throw new SimplifiedException("无法识别的数字格式");
                    h = tmp.startsWith("十") ? 1L : (parts[0].length() > 1 ? Long.parseLong(parts[0]) : numMap.get(parts[0]));
                    tmp = parts.length > 1 ? parts[1] : null;
                    resN += h * 10L;
                    break;
                default:
                    if (!numMap.containsKey(tmp))
                        throw new SimplifiedException("无法识别的数字格式");
                    resN += numMap.get(tmp);
                    tmp = null;
            }
        }
        return resN;
    }

    private static String lookupScale(String numU) {
        for (String s : scales) {
            if (numU.contains(s))
                return s;
        }
        return "";
    }

}
