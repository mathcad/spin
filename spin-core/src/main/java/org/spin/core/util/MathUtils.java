package org.spin.core.util;

import org.spin.core.ErrorCode;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SpinException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数学工具类
 * <p>提供基础的数学工具，运算等方法</p>
 * <p>Created by xuweinan on 2018/11/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class MathUtils extends Util {

    private MathUtils() {
    }

    /**
     * 平方根运算
     *
     * @param number       运算数
     * @param scale        精度
     * @param roundingMode 舍入规则
     * @return 平方根
     */
    public static BigDecimal sqrt(BigDecimal number, int scale, RoundingMode roundingMode) {
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
        BigDecimal fraction = number.subtract(number.setScale(0, RoundingMode.DOWN));
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

    /**
     * 平方根运算（四舍五入）
     *
     * @param number 运算数
     * @param scale  精度
     * @return 平方根
     */
    public static BigDecimal sqrt(BigDecimal number, int scale) {
        return sqrt(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 平方根运算（精度比原始值多两位，最多50位；四舍五入）
     *
     * @param number 运算数
     * @return 平方根
     */
    public static BigDecimal sqrt(BigDecimal number) {
        int scale = number.scale() * 2;
        if (scale < 50)
            scale = 50;
        return sqrt(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 加法运算
     *
     * @param values 数值
     * @return 和
     */
    public static BigDecimal add(Number... values) {
        BigDecimal res = BigDecimal.ZERO;
        for (Number value : values) {
            res = res.add(NumericUtils.toBigDeciaml(value));
        }
        return res;
    }

    /**
     * 乘法运算
     *
     * @param values 数值
     * @return 积
     */
    public static BigDecimal multiply(Number... values) {
        BigDecimal res = BigDecimal.ONE;
        for (Number value : values) {
            res = res.multiply(NumericUtils.toBigDeciaml(value));
        }
        return res;
    }

    /**
     * 减法运算
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 差
     */
    public static BigDecimal subtract(Number value1, Number value2) {
        return NumericUtils.toBigDeciaml(value1).subtract(NumericUtils.toBigDeciaml(value2));
    }

    /**
     * 除法运算
     *
     * @param value1 被除数
     * @param value2 除数
     * @return 商
     */
    public static BigDecimal divide(Number value1, Number value2) {
        BigDecimal v2 = NumericUtils.toBigDeciaml(value2);
        if (v2.compareTo(BigDecimal.ZERO) == 0) {
            throw new SpinException(ErrorCode.INVALID_PARAM, "除数不能为0");
        }
        return NumericUtils.toBigDeciaml(value1).divide(NumericUtils.toBigDeciaml(value2), RoundingMode.HALF_UP);
    }

    /**
     * 统计某数值列的汇总
     * <pre>
     * 空值会被过滤，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal sum(Collection<T> list, Function<T, P> propGetter) {
        return list.stream().map(propGetter).filter(Objects::nonNull).map(NumericUtils::toBigDeciaml).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    /**
     * 统计某数值列的汇总
     * <pre>
     * 空值会被过滤，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal sum(Collection<Map<K, V>> list, K key) {
        return list.stream().map(m -> MapUtils.getBigDecimalValue(m, key)).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 统计某数值列最大值
     * <pre>
     * 空值会被过滤，如果全部为null，返回null
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal max(Collection<T> list, Function<T, P> propGetter) {
        return list.stream().map(propGetter).filter(Objects::nonNull).map(NumericUtils::toBigDeciaml).max(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列最大值
     * <pre>
     * 空值会被过滤，如果全部为null，返回null
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal max(Collection<Map<K, V>> list, K key) {
        return list.stream().map(m -> MapUtils.getBigDecimalValue(m, key)).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列最小值
     * <pre>
     * 空值会被过滤，如果全部为null，返回null
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal min(Collection<T> list, Function<T, P> propGetter) {
        return list.stream().map(propGetter).filter(Objects::nonNull).map(NumericUtils::toBigDeciaml).min(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列最小值
     * <pre>
     * 空值会被过滤，如果全部为null，返回null
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal min(Collection<Map<K, V>> list, K key) {
        return list.stream().map(m -> MapUtils.getBigDecimalValue(m, key)).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null);
    }

    /**
     * 统计某数值列的数学期望
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal mean(Collection<T> list, Function<T, P> propGetter) {
        BigDecimal total = sum(list, propGetter);
        return total.divide(new BigDecimal(list.size()), RoundingMode.HALF_UP);
    }

    /**
     * 统计某数值列的数学期望
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal mean(Collection<Map<K, V>> list, K key) {
        BigDecimal total = sum(list, key);
        return total.divide(new BigDecimal(list.size()), RoundingMode.HALF_UP);
    }

    /**
     * 统计某数值列的几何平均数
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal geometricMean(Collection<T> list, Function<T, P> propGetter) {
        return BigDecimal.valueOf(Math.pow(list.stream()
            .map(propGetter)
            .map(NumericUtils::toBigDeciaml)
            .reduce(BigDecimal::multiply)
            .orElse(BigDecimal.ZERO).doubleValue(), 1.0D / list.size()));
    }

    /**
     * 统计某数值列的几何平均数
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal geometricMean(Collection<Map<K, V>> list, K key) {
        return BigDecimal.valueOf(Math.pow(list.stream()
            .map(m -> MapUtils.getBigDecimalValue(m, key))
            .map(NumericUtils::toBigDeciaml)
            .reduce(BigDecimal::multiply)
            .orElse(BigDecimal.ZERO).doubleValue(), 1.0D / list.size()));
    }


    /**
     * 统计某数值列的平方平均数
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal quadraticMean(Collection<T> list, Function<T, P> propGetter) {
        return list.stream().map(propGetter)
            .map(NumericUtils::toBigDeciaml)
            .map(n -> n.multiply(n))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .divide(BigDecimal.valueOf(list.size()), RoundingMode.HALF_UP);
    }

    /**
     * 统计某数值列的平方平均数
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal quadraticMean(Collection<Map<K, V>> list, K key) {
        return list.stream().map(m -> MapUtils.getBigDecimalValue(m, key))
            .map(NumericUtils::toBigDeciaml)
            .map(n -> n.multiply(n))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .divide(BigDecimal.valueOf(list.size()), RoundingMode.HALF_UP);
    }


    /**
     * 统计某数值列的总体方差
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal variance(Collection<T> list, Function<T, P> propGetter) {
        if (CollectionUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> nums = list.stream().map(propGetter).map(NumericUtils::toBigDeciaml).collect(Collectors.toList());

        BigDecimal avg = nums.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);

        return nums.stream().map(n -> n.subtract(avg).pow(2))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);
    }

    /**
     * 统计某数值列的总体方差
     * <pre>
     * 空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal variance(Collection<Map<K, V>> list, K key) {
        if (CollectionUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> nums = list.stream().map(map -> MapUtils.getBigDecimalValue(map, key)).collect(Collectors.toList());
        BigDecimal avg = nums.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);


        return nums.stream().map(n -> n.subtract(avg).pow(2))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);
    }

    /**
     * 统计样本集合某数值列的样本方差（无偏方差），样本数不能小于2
     * <pre>
     *     1.样本数小于2时，返回null
     *     2.空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list       列表
     * @param propGetter 统计字段的getter
     * @param <T>        集合元素类型参数
     * @param <P>        getter属性的类型参数
     * @return 统计结果
     */
    public static <T, P> BigDecimal unbiasedVariance(Collection<T> list, Function<T, P> propGetter) {
        if (CollectionUtils.isEmpty(list) || list.size() < 2) {
            return null;
        }

        List<BigDecimal> nums = list.stream().map(propGetter).map(NumericUtils::toBigDeciaml).collect(Collectors.toList());


        BigDecimal avg = nums.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);

        return nums.stream().map(n -> n.subtract(avg).pow(2))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size() - 1), RoundingMode.HALF_UP);
    }

    /**
     * 统计样本集合某数值列的样本方差（无偏方差），样本数不能小于2
     * <pre>
     *     1.样本数小于2时，返回null
     *     2.空值会被当做0参与统计，如果全部为null，返回0
     * </pre>
     *
     * @param list 列表
     * @param key  统计字段
     * @param <K>  map的key类型
     * @param <V>  map的值类型
     * @return 统计结果
     */
    public static <K, V> BigDecimal unbiasedVariance(Collection<Map<K, V>> list, K key) {
        if (CollectionUtils.isEmpty(list) || list.size() < 2) {
            return null;
        }

        List<BigDecimal> nums = list.stream().map(map -> MapUtils.getBigDecimalValue(map, key)).collect(Collectors.toList());

        BigDecimal avg = nums.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size()), RoundingMode.HALF_UP);

        return nums.stream().map(n -> n.subtract(avg).pow(2))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(new BigDecimal(nums.size() - 1), RoundingMode.HALF_UP);
    }
}
