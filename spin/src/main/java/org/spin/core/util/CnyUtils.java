package org.spin.core.util;

import org.spin.core.throwable.SimplifiedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CnyUtils {
    private static Pattern numPattern = Pattern.compile("^([-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]+)[^-负0-9零一壹二贰两俩三叁四肆五伍六陆七柒八捌九玖十拾百佰千仟万点.]{0,2}$");
    private static Pattern pureNum = Pattern.compile("^-?\\d+$|^\\d+\\.\\d+$");
    private static String[] scales = {"亿", "万", "千", "仟", "百", "佰", "十", "拾"};
    private static Map<String, Long> numMap = new HashMap<>();

    /**
     * 中文简体数字
     */
    private static String CHS_NUMBER = "零一二三四五六七八九";

    /**
     * 中文繁体数字
     */
    private static String CHT_NUMBER = "零壹贰叁肆伍陆柒捌玖";

    /**
     * 汉语中数字大写
     */
    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    /**
     * 汉语中货币单位大写，这样的设计类似于占位符
     */
    private static final String[] CN_UPPER_MONETRAY_UNIT = {"分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};
    /**
     * 特殊字符：整
     */
    private static final String CN_FULL = "整";
    /**
     * 特殊字符：负
     */
    private static final String CN_NEGATIVE = "负";
    /**
     * 金额的精度，默认值为2
     */
    private static final int MONEY_PRECISION = 2;
    /**
     * 特殊字符：零元整
     */
    private static final String CN_ZEOR_FULL = "零元" + CN_FULL;

    static {
        numMap.put("零", 0L);
        numMap.put("一", 1L);
        numMap.put("壹", 1L);
        numMap.put("二", 2L);
        numMap.put("贰", 2L);
        numMap.put("两", 2L);
        numMap.put("俩", 2L);
        numMap.put("三", 3L);
        numMap.put("叁", 3L);
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
     * 分析数字，将中文数字转换为阿拉伯数字
     * 兼容中文与数字混合，只要数字语义正确即可
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


    /**
     * 把输入的金额转换为汉语中人民币的大写
     *
     * @param money 输入的金额
     * @return 对应的汉语大写
     */
    public static String convertCNY(String money) {
        BigDecimal numberOfMoney = BigDecimal.valueOf(Double.valueOf(money));
        StringBuilder sb = new StringBuilder();
        // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
        // positive.
        int signum = numberOfMoney.signum();
        // 零元整的情况
        if (signum == 0) {
            return CN_ZEOR_FULL;
        }
        // 这里会进行金额的四舍五入
        long number = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit;
        int numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四种情况：00 = 0, 01 = 1, 10, 11
        if (!(scale > 0)) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if ((scale > 0) && (!(scale % 10 > 0))) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (true) {
            if (number <= 0) {
                break;
            }
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[10]);
                }
                sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!(getZero)) {
                    sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                }
                getZero = true;
            }
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
        }
        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            sb.insert(0, CN_NEGATIVE);
        }
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
        if (!(scale > 0)) {
            sb.append(CN_FULL);
        }
        return sb.toString();
    }

    /**
     * 中文数字转化
     */
    public static String convertNum(String number) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            res.append(CHT_NUMBER.charAt(number.charAt(i) - '0'));
        }
        return res.toString();
    }

    /**
     * 中文金额转化
     */
    public static String convertCNYByCHT(String money) {
        StringBuilder rsult = new StringBuilder();
        String unit = "仟佰拾亿仟佰拾万仟佰拾元角分";
        String moneyStr = money.replace(".", "");
        int pos = unit.length() - moneyStr.length();
        boolean zero = false;
        for (int i = 0; i < moneyStr.length(); i++) {
            if (moneyStr.charAt(i) == '0') {
                zero = true;
                if (((pos + i + 1) % 4) == 0) {
                    rsult.append(unit.charAt(pos + i));
                    zero = false;
                }
            } else {
                if (zero) {
                    rsult.append(CHT_NUMBER.charAt(0));
                }
                zero = false;
                rsult.append(CHT_NUMBER.charAt(moneyStr.charAt(i) - '0')).append(unit.charAt(pos + i));
            }
        }
        if (moneyStr.endsWith("00")) {
            rsult.append('整');
        } else if (moneyStr.endsWith("0")) {
            rsult.append("零分");
        }
        return rsult.toString();
    }
}
