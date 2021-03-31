package org.spin.jpa.lin;

import org.spin.jpa.Prop;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

/**
 * 语言集成更新
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/3/29</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface Linu<T> extends Lin<Linu<T>, CriteriaUpdate<?>> {

    /**
     * 批量更新
     *
     * @return 更新记录数
     */
    int update();

    /**
     * 设置更新值
     *
     * @param attribute 属性
     * @param value     值
     * @param <Y>       范型
     * @return 本身
     */
    <Y> Linu<T> set(Path<Y> attribute, Expression<? extends Y> value);

    /**
     * 设置更新值
     *
     * @param attributeName 属性名称
     * @param value         值
     * @return 本身
     */
    Linu<T> set(String attributeName, Object value);

    Linu<T> set(Prop<T, ?> attribute, Object value);

    /**
     * 设置更新值
     *
     * @param attribute 属性
     * @param value     值
     * @param <Y>       路径范型
     * @param <X>       值范型
     * @return 本身
     */
    <Y, X extends Y> Linu<T> set(Path<Y> attribute, X value);

    /**
     * 设置更新值
     *
     * @param attribute 属性
     * @param value     值
     * @param <Y>       属性范型
     * @param <X>       值范型
     * @return 本身
     */
    <Y, X extends Y> Linu<T> set(SingularAttribute<? super Object, Y> attribute, X value);

    /**
     * 设置更新值
     *
     * @param attribute 属性
     * @param value     值
     * @param <Y>       属性范型
     * @return 本身
     */
    <Y> Linu<T> set(SingularAttribute<? super Object, Y> attribute, Expression<? extends Y> value);
}
