package org.infrastructure.sys;

import org.infrastructure.throwable.BizException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 类型计算
 *
 * @author lgh
 * @create 2016年6月4日 上午9:52:50
 */
public class DecimalUtils {

    /**
     * 加法,精度为参数精度最大值
     *
     * @param value
     * @return
     * @version 1.0
     */
    public static BigDecimal add(BigDecimal... value) {
        BigDecimal all = BigDecimal.ZERO;

        if (value == null)
            return all;

        for (BigDecimal d : value)
            if (d != null)
                all = all.add(d);
        return all;
    }

    /**
     * 减法,精度为参数精度最大值
     *
     * @param value
     * @return
     * @version 1.0
     */
    public static BigDecimal subtraction(BigDecimal... value) {
        BigDecimal all = BigDecimal.ZERO;

        if (value == null)
            return all;

        for (int i = 0; i < value.length; i++) {
            if (i == 0) {
                all = value[i];
                continue;
            }
            BigDecimal d = value[i];
            if (d != null)
                all = all.subtract(d);
        }

        return all;
    }

    /**
     * 乘法,精度为乘数精度只和
     *
     * @param value
     * @return
     * @version 1.0
     */
    public static BigDecimal multiplication(BigDecimal... value) {
        BigDecimal all = BigDecimal.ZERO;

        if (value == null)
            return all;
        if (value.length < 2)
            throw new BizException("参数个数不能少于2个");

        for (int i = 0; i < value.length; i++) {
            if (i == 0) {
                all = value[i];
                continue;
            }

            BigDecimal d = value[i];
            if (d != null)
                all = all.multiply(d);
        }

        return all;
    }

    /**
     * 除法,默认精度6位
     *
     * @return
     * @version 1.0
     */
    public static BigDecimal division(BigDecimal... value) {
        return division(6, value);
    }

    /**
     * 除法
     *
     * @return
     * @version 1.0
     */
    public static BigDecimal division(int scale, BigDecimal... value) {
        BigDecimal all = BigDecimal.ZERO;

        if (value == null)
            return all;

        for (int i = 0; i < value.length; i++) {
            if (i == 0) {
                all = value[i];
                continue;
            }

            BigDecimal d = value[i];
            if (d != null)
                if (BigDecimal.ZERO.compareTo(d) == 0)
                    throw new BizException("除数不可以为0");
                else
                    all = all.divide(d, scale, RoundingMode.HALF_UP);
        }
        return all;
    }

    /**
     * 比较
     * start> end: 1
     * start< end: -1
     * start= end: 0
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static int compare(BigDecimal start, BigDecimal end) {
        return start.compareTo(end);
    }

    /**
     * 0与指定指比较(0>X 返回 1, 0=X 返回 0, 0<X 返回 -1)
     * value < 0 : 1
     * value > 0 : -1
     * value = 0 : 0
     *
     * @param value
     * @return
     * @version 1.0
     */
    public static int compareZero(BigDecimal value) {
        return compare(BigDecimal.ZERO, value);
    }

    /**
     * 大于
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static boolean gt(BigDecimal start, BigDecimal end) {
        return compare(start, end) > 0;
    }

    /**
     * 大于等于
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static boolean ge(BigDecimal start, BigDecimal end) {
        return compare(start, end) > -1;
    }

    /**
     * 小于等于
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static boolean le(BigDecimal start, BigDecimal end) {
        return compare(start, end) < 1;
    }

    /**
     * 小与
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static boolean lt(BigDecimal start, BigDecimal end) {
        return compare(start, end) < 0;
    }

    /**
     * 小与
     *
     * @param start
     * @param end
     * @return
     * @version 1.0
     */
    public static boolean eq(BigDecimal start, BigDecimal end) {
        return compare(start, end) == 0;
    }

    /**
     * 绝对值
     *
     * @param decimal
     * @return
     * @version 1.0
     */
    public static BigDecimal abs(BigDecimal decimal) {
        if (lt(decimal, BigDecimal.ZERO))
            return decimal.negate();
        else
            return decimal;
    }
}

