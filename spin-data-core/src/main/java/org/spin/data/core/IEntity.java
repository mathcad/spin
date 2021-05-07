package org.spin.data.core;

import org.spin.core.util.BeanUtils;
import org.spin.core.util.ReflectionUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * 基础实体接口
 * <p>所有实体均直接或间接实现此接口。只有此接口的实现类才可以被持久化</p>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @param <PK> 主键类型
 * @param <E>  实际实体类型
 * @author xuweinan
 * @version 1.1
 */
public interface IEntity<PK extends Serializable, E extends IEntity<PK, E>> extends Serializable {

    /**
     * 主键
     *
     * @return 主键
     */
    PK id();

    /**
     * 主键
     *
     * @param id 主键
     */
    void id(PK id);

    /**
     * 数据版本 乐观锁
     *
     * @return 数据版本
     */
    Integer getVersion();

    /**
     * 数据版本 乐观锁
     *
     * @param version 数据版本
     */
    void setVersion(Integer version);

    /**
     * 是否有效 逻辑删除标记
     *
     * @return 是否有效
     */
    Boolean getValid();

    /**
     * 是否有效 逻辑删除标记
     *
     * @param valid 是否有效
     */
    void setValid(Boolean valid);

    /**
     * 将当前实体中的属性copy到目标对象中(所有存在于目标中的属性)
     *
     * @param target 目标对象
     * @param <T>    目标对象类型
     * @return 拷贝属性后的目标对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default <T> T copyTo(T target) {
        if (null != target) {
            if (target instanceof Map) {
                ReflectionUtils.doWithFields(this.getClass(), field -> {
                    ReflectionUtils.makeAccessible(field);
                    ((Map) target).put(field.getName(), ReflectionUtils.getField(field, this));
                });
            } else {
                BeanUtils.copyTo(this, target);
            }
        }
        return target;
    }

    /**
     * 将指定对象中的所有非空属性copy到当前对象中(所有存在于当前对象中的属性)
     *
     * @param source 源对象
     * @param <T>    源对象类型
     * @return 当前对象
     */
    @SuppressWarnings("unchecked")
    default <T> E merge(T source) {
        BeanUtils.copyTo(source, this, null, (f, v) -> null != v, null);
        return (E) this;
    }

    /**
     * 将指定对象中的所有属性copy到当前对象中(所有存在于当前对象中的属性)
     *
     * @param source 源对象
     * @param <T>    源对象类型
     * @return 当前对象
     */
    @SuppressWarnings("unchecked")
    default <T> E mergeAll(T source) {
        BeanUtils.copyTo(source, this);
        return (E) this;
    }
}
