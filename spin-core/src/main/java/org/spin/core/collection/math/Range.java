package org.spin.core.collection.math;

import org.spin.core.Assert;
import org.spin.core.collection.Pair;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 区间范围表示
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/2/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Range<T extends Comparable<T>> implements Comparable<Range<T>> {
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^[(|\\[]\\d+,\\d*[)\\]]$");

    private final T begin;

    private final boolean inclusiveBegin;

    private final T end;

    private final boolean inclusiveEnd;

    public static Range<BigDecimal> parseBigDecimalRange(String expression) {
        return parseRange(expression, BigDecimal::new);

    }

    public static Range<Long> parseLongRange(String expression) {
        return parseRange(expression, Long::parseLong);
    }

    public static Range<Integer> parseIntRange(String expression) {
        return parseRange(expression, Integer::parseInt);
    }

    public static <T extends Comparable<T>> Range<T> parseRange(String expression, Function<String, T> converter) {
        expression = StringUtils.trimToEmpty(expression).replaceAll("\\s", "");
        Assert.isTrue(EXPRESSION_PATTERN.matcher(expression).matches(), "表达式不合法");

        boolean inclusiveBegin = expression.startsWith("[");
        boolean inclusiveEnd = expression.endsWith("]");

        expression = expression.substring(1, expression.length() - 1);

        String[] r = expression.split(",");

        T begin = converter.apply(r[0]);

        T end = null;
        if (r.length == 2) {
            end = converter.apply(r[1]);
        }
        return new Range<>(begin, inclusiveBegin, end, inclusiveEnd);
    }

    public static <T extends Comparable<T>> List<Range<T>> checkRanges(List<Range<T>> rangeList) {
        if (CollectionUtils.isEmpty(rangeList)) {
            return rangeList;
        }

        rangeList.sort(Range::compareTo);
        Assert.notNull(rangeList.get(0).end, "结束区间的终点不能为空");
        for (int i = 1; i < rangeList.size(); i++) {
            Range<T> range = rangeList.get(i);
            if (i != rangeList.size() - 1) {
                Assert.notNull(range.end, "结束区间的终点不能为空");
            }
            Assert.ge(range.begin, rangeList.get(i - 1).end, "后一区间的起点不能小于前一区间的终点");
        }

        return rangeList;
    }

    public static <T extends Comparable<T>> Pair<T, Range<T>> detectRange(T value, List<Range<T>> rangeList) {
        Assert.notNull(value, "value不能为空");
        for (Range<T> range : rangeList) {
            int b = value.compareTo(range.begin);
            int e = null == range.end ? -1 : value.compareTo(range.end);

            if ((b > 0 || range.inclusiveBegin && b == 0) && (e < 0 || range.inclusiveEnd && e == 0)) {
                return Pair.of(value, range);
            }
        }

        return Pair.of(value, null);
    }

    public Range(T begin, boolean inclusiveBegin, T end, boolean inclusiveEnd) {
        Assert.notNull(begin, "区间起点不能为空");
        if (null != end) {
            Assert.gt(end, begin, "区间终点数值必须大于起点");
        }
        this.begin = begin;
        this.inclusiveBegin = inclusiveBegin;
        this.end = end;
        this.inclusiveEnd = inclusiveEnd;
    }

    public T getBegin() {
        return begin;
    }

    public boolean isInclusiveBegin() {
        return inclusiveBegin;
    }

    public T getEnd() {
        return end;
    }

    public boolean isInclusiveEnd() {
        return inclusiveEnd;
    }

    public String toSimpleRangeString() {
        return begin.toString() + "" + (null == end ? "以上" : ("-" + end));
    }

    @Override
    public int compareTo(Range<T> o) {
        return this.begin.compareTo(o.begin);
    }

    @Override
    public String toString() {
        return (inclusiveBegin ? "[" : "(") + begin.toString() + "," + (null == end ? "" : end.toString()) + (inclusiveEnd ? "]" : ")");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range<?> range = (Range<?>) o;
        return inclusiveBegin == range.inclusiveBegin && inclusiveEnd == range.inclusiveEnd && begin.equals(range.begin) && Objects.equals(end, range.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, inclusiveBegin, end, inclusiveEnd);
    }
}
