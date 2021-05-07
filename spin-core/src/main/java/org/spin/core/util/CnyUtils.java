package org.spin.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CnyUtils extends Util {

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

    private CnyUtils() {
    }

    /**
     * 把输入的金额转换为汉语中人民币的大写
     *
     * @param money 输入的金额
     * @return 对应的汉语大写
     */
    public static String convertToCNY(String money) {
        BigDecimal numberOfMoney = new BigDecimal(money);
        return convertToCNY(numberOfMoney);
    }

    /**
     * 把输入的金额转换为汉语中人民币的大写
     *
     * @param money 输入的金额
     * @return 对应的汉语大写
     */
    public static String convertToCNY(BigDecimal money) {
        StringBuilder sb = new StringBuilder();
        // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
        // positive.
        int signum = money.signum();
        // 零元整的情况
        if (signum == 0) {
            return CN_ZEOR_FULL;
        }
        // 这里会进行金额的四舍五入
        long number = money.movePointRight(MONEY_PRECISION).setScale(0, RoundingMode.HALF_UP).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit;
        int numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四种情况：00 = 0, 01 = 1, 10, 11
        if (scale == 0) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        } else if (scale % 10 == 0) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (number > 0) {
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
            if (numUnit == 0) {

                ++zeroSize;
                if (!getZero) {
                    sb.append(CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2 || ((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.append(CN_UPPER_MONETRAY_UNIT[numIndex]);
                }
                getZero = true;
            } else {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    sb.append(CN_UPPER_MONETRAY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    sb.append(CN_UPPER_MONETRAY_UNIT[10]);
                }
                sb.append(CN_UPPER_MONETRAY_UNIT[numIndex]);
                sb.append(CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;

            }
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
        }
        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            sb.append(CN_NEGATIVE);
        }
        sb.reverse();
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
        if (scale == 0) {
            sb.append(CN_FULL);
        }
        return sb.toString();
    }

    /**
     * 把输入的汉语人民币的大写转换为数字金额
     *
     * @param money 人民币的大写
     * @return 数字金额
     */
    public static BigDecimal convertFromCNY(String money) {
        if (StringUtils.isEmpty(money)) {
            return null;
        }

        if (CN_ZEOR_FULL.equals(money)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String integer;
        String decimal;
        if (money.endsWith(CN_FULL)) {
            integer = money.substring(0, money.length() - 1);
            decimal = "";
        } else {
            int spliterIdx = money.indexOf('元');
            if (spliterIdx > 0) {
                integer = money.substring(0, spliterIdx);
                decimal = money.substring(spliterIdx + 1);
            } else {
                if (money.startsWith(CN_NEGATIVE)) {
                    integer = CN_NEGATIVE;
                    decimal = money.substring(1);
                } else {
                    integer = "";
                    decimal = money;
                }
                integer += "零";
            }
        }

        if (decimal.length() > 0) {
            decimal = "点" + decimal.charAt(0) + (decimal.length() > 2 ? decimal.charAt(2) : "");
        }
        return new BigDecimal(NumericUtils.analysisNumber(integer + decimal)).setScale(2, RoundingMode.HALF_UP);
    }
}
