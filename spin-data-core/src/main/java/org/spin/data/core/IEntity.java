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
 * @author xuweinan
 * @version 1.1
 */
public interface IEntity<PK extends Serializable> extends Serializable {

    PK getId();

    void setId(PK id);

    int getVersion();

    void setVersion(int version);

    boolean isValid();

    void setValid(boolean valid);

    /**
     * 为当前实体指定ID并返回当前实体
     *
     * @param id  id
     * @param <E> 当前实体类型
     * @return 当前实体
     */
    @SuppressWarnings("unchecked")
    default <E extends IEntity<PK>> E withId(PK id) {
        this.setId(id);
        return (E) this;
    }

    /**
     * 将当前实体中的属性copy到目标对象中(所有存在于目标中的属性)
     *
     * @param target 目标对象
     * @param <T>    目标对象类型
     * @return 拷贝属性后的目标对象
     */
    default <T> T copyTo(T target) {
        if (null != target) {
            if (target instanceof Map) {
                ReflectionUtils.doWithFields(this.getClass(), field -> {
                    ReflectionUtils.makeAccessible(field);
                    //noinspection unchecked
                    ((Map) target).put(field.getName(), ReflectionUtils.getField(field, this));
                });
            } else {
                BeanUtils.copyTo(this, target);
            }
        }
        return target;
    }

    /**
     * 根据id,获取一个持有该id的指定类型的DTO对象
     *
     * @param entityCls 实体类型
     * @param id        id
     * @param <PK>      主键类型泛型参数
     * @param <E>       实体类型泛型参数
     * @return 持有指定id的DTO对象
     */
    static <PK extends Serializable, E extends IEntity<PK>> E ref(Class<E> entityCls, PK id) {
        E entity;
        entity = BeanUtils.instantiateClass(entityCls);
        entity.setId(id);
        return entity;
    }
}
