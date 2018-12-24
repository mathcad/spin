package org.spin.core.util;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数学工具类
 * <p>提供基础的数学工具，运算等方法</p>
 * <p>Created by xuweinan on 2018/11/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MathUtils {

    public static BigDecimal sqrt(BigDecimal number, int scale, int roundingMode) {
        if (number.compareTo(BigDecimal.ZERO) < 0)
            throw new ArithmeticException("sqrt with negative");
        BigInteger integer = number.toBigInteger();
        StringBuilder sb = new StringBuilder();
        String strInt = integer.toString();
        int lenInt = strInt.length();
        if (lenInt % 2 != 0) {
            strInt = '0' + strInt;
            lenInt++;
        }
        BigInteger res = BigInteger.ZERO;
        BigInteger rem = BigInteger.ZERO;
        BigInteger hundred = BigInteger.valueOf(100);
        for (int i = 0; i < lenInt / 2; i++) {
            res = res.multiply(BigInteger.TEN);
            rem = rem.multiply(hundred);

            BigInteger temp = new BigInteger(strInt.substring(i * 2, i * 2 + 2));
            rem = rem.add(temp);

            BigInteger j = BigInteger.TEN;
            while (j.compareTo(BigInteger.ZERO) > 0) {
                j = j.subtract(BigInteger.ONE);
                if (((res.add(j)).multiply(j)).compareTo(rem) <= 0) {
                    break;
                }
            }

            res = res.add(j);
            rem = rem.subtract(res.multiply(j));
            res = res.add(j);
            sb.append(j);
        }
        sb.append('.');
        BigDecimal fraction = number.subtract(number.setScale(0, BigDecimal.ROUND_DOWN));
        int fracLen = (fraction.scale() + 1) / 2;
        fraction = fraction.movePointRight(fracLen * 2);
        String strFrac = fraction.toPlainString();
        for (int i = 0; i <= scale; i++) {
            res = res.multiply(BigInteger.TEN);
            rem = rem.multiply(hundred);

            if (i < fracLen) {
                BigInteger temp = new BigInteger(strFrac.substring(i * 2, i * 2 + 2));
                rem = rem.add(temp);
            }

            BigInteger j = BigInteger.TEN;
            while (j.compareTo(BigInteger.ZERO) > 0) {
                j = j.subtract(BigInteger.ONE);
                if (((res.add(j)).multiply(j)).compareTo(rem) <= 0) {
                    break;
                }
            }
            res = res.add(j);
            rem = rem.subtract(res.multiply(j));
            res = res.add(j);
            sb.append(j);
        }
        return new BigDecimal(sb.toString()).setScale(scale, roundingMode);
    }

    public static BigDecimal sqrt(BigDecimal number, int scale) {
        return sqrt(number, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal sqrt(BigDecimal number) {
        int scale = number.scale() * 2;
        if (scale < 50)
            scale = 50;
        return sqrt(number, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal add(Number... values) {
        BigDecimal res = BigDecimal.ZERO;
        for (Number value : values) {
            res = res.add(NumericUtils.toBigDeciaml(value));
        }
        return res;
    }

    public static BigDecimal multiply(Number... values) {
        BigDecimal res = BigDecimal.ONE;
        for (Number value : values) {
            res = res.multiply(NumericUtils.toBigDeciaml(value));
        }
        return res;
    }

    public static BigDecimal subtract(Number value1, Number value2) {
        return NumericUtils.toBigDeciaml(value1).subtract(NumericUtils.toBigDeciaml(value2));
    }

    public static BigDecimal divide(Number value1, Number value2) {
        BigDecimal v2 = NumericUtils.toBigDeciaml(value2);
        if (v2.compareTo(BigDecimal.ZERO) == 0) {
            throw new SimplifiedException(ErrorCode.INVALID_PARAM, "除数不能为0");
        }
        return NumericUtils.toBigDeciaml(value1).subtract(NumericUtils.toBigDeciaml(value2));
    }
}
