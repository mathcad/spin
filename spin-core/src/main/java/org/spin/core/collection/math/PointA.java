package org.spin.core.collection.math;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 基于笛卡尔坐标系的点
 *
 * @param <T> 坐标数值类型
 * @param <P> Point类型
 */
public interface PointA<T extends Number, P extends PointA> extends Serializable {

    int X = 1;
    int Y = 2;
    int Z = 3;

    /**
     * 维度
     *
     * @return 维度
     */
    int dimension();

    /**
     * 两点间距离
     *
     * @param another 目标点
     * @return 欧氏距离
     */
    BigDecimal distance(PointA another);

    /**
     * 两点间距离
     *
     * @return 距离原点的欧氏距离
     */
    BigDecimal distance();

    /**
     * 向指定坐标轴投影
     *
     * @param axis 坐标轴
     * @return 投影后的值
     */
    T projection(int axis);

    /**
     * 将该点向指定方向偏移
     *
     * @param point 偏移量
     * @return 偏移后的点
     */
    P offset(PointA point);
}
