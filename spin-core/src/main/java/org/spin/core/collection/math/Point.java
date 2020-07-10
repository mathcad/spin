package org.spin.core.collection.math;

import org.spin.core.Assert;
import org.spin.core.util.MathUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * 内积空间中的点
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public class Point {
    private static final long serialVersionUID = 7900633074310166225L;

    public final BigDecimal[] coords;
    public final BigDecimal d;
    public final int scale;

    /**
     * 通过数值构造一个点（使用浮点类型进行计算可能会产生精度损失）
     *
     * @param coords 坐标序列
     */
    public Point(Collection<? extends Number> coords) {
        this.coords = new BigDecimal[Assert.notNull(coords, "坐标序列不能为空").size()];
        int scale = 0;
        BigDecimal sum = BigDecimal.ZERO;
        int i = 0;
        for (Number c : coords) {
            BigDecimal coord = null == c ? BigDecimal.ZERO : (c instanceof BigDecimal ? (BigDecimal) c : BigDecimal.valueOf(c.doubleValue()));
            this.coords[i] = coord;
            if (coord.scale() > scale) {
                scale = coord.scale();
            }
            sum = sum.add(coord.pow(2));
            ++i;
        }
        this.scale = scale;
        this.d = MathUtils.sqrt(sum, scale * 2);
    }

    /**
     * 通过数值构造一个点（使用浮点类型进行计算可能会产生精度损失）
     *
     * @param coords 坐标序列
     */
    public Point(Number... coords) {
        this.coords = new BigDecimal[coords.length];
        int scale = 0;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < coords.length; i++) {
            BigDecimal coord = null == coords[i] ? BigDecimal.ZERO : (coords[i] instanceof BigDecimal ? (BigDecimal) coords[i] : BigDecimal.valueOf(coords[i].doubleValue()));
            this.coords[i] = coord;
            if (coord.scale() > scale) {
                scale = coord.scale();
            }
            sum = sum.add(coord.pow(2));
        }
        this.scale = scale;
        this.d = MathUtils.sqrt(sum, scale * 2);
    }

    /**
     * 维度
     *
     * @return 维度
     */
    public int dimension() {
        return coords.length;
    }

    /**
     * 坐标精度
     *
     * @return 精度
     */
    public int getScale() {
        return scale;
    }

    /**
     * 两点间距离
     *
     * @param point 目标点
     * @return 欧氏距离
     */
    public BigDecimal distance(Point point) {
        if (null == point) {
            return d;
        }
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal[] coords;
        BigDecimal[] shorter;
        int scale = Math.max(this.scale, point.scale);
        if (point.dimension() > dimension()) {
            coords = Arrays.copyOf(point.coords, point.dimension());
            shorter = this.coords;
        } else {
            coords = Arrays.copyOf(this.coords, point.dimension());
            shorter = point.coords;
        }
        for (int i = 0; i != shorter.length; ++i) {
            sum = sum.add(coords[i].subtract(shorter[i]).pow(2));
        }
        for (int i = shorter.length; i != coords.length; ++i) {
            sum = sum.add(coords[i].pow(2));
        }
        return MathUtils.sqrt(sum, scale);
    }

    /**
     * 两点间距离
     *
     * @return 距离原点的欧氏距离
     */
    public BigDecimal distance() {
        return d;
    }

    /**
     * 指定维上的投影分量
     *
     * @param axis 维度
     * @return 指定维上的投影分量（维数从1开始）
     */
    public BigDecimal projection(int axis) {
        return coords[Assert.inclusiveBetween(1, dimension(), axis, "指定的坐标轴超出维数") - 1];
    }

    /**
     * 将该点向指定方向偏移
     *
     * @param point 偏移量
     * @return 偏移后的点
     */
    public Point offset(Point point) {
        BigDecimal[] coords;
        if (null == point) {
            coords = Arrays.copyOf(this.coords, dimension());
        } else {
            BigDecimal[] shorter;
            if (point.dimension() > dimension()) {
                coords = Arrays.copyOf(point.coords, point.dimension());
                shorter = this.coords;
            } else {
                coords = Arrays.copyOf(this.coords, point.dimension());
                shorter = point.coords;
            }
            for (int i = 0; i < shorter.length; i++) {
                coords[i] = coords[i].add(shorter[i]);
            }
        }
        return new Point(coords);
    }
}
