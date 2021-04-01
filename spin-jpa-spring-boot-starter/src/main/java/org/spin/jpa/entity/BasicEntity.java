package org.spin.jpa.entity;

import org.spin.core.Assert;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.core.util.BeanUtils;
import org.spin.data.core.IEntity;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Objects;

/**
 * 基础实体
 * <p>定义了数据库实体的基本字段, 原则上所有实体均应直接或间接继承{@link BasicEntity}</p>
 * <p>Created by xuweinan on 2019/9/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@MappedSuperclass
public abstract class BasicEntity<T extends BasicEntity<T>> implements IEntity<Long, T>, Serializable {

    // region properties
    /**
     * 主键
     */
    @Id
    @PreventOverflow
    private Long id;

    /**
     * 版本号用于并发控制
     */
    @Version
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public void id(Long id) {
        this.id = id;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public Boolean getValid() {
        return true;
    }

    @Override
    public void setValid(Boolean valid) {
        // do nothing
    }
    // endregion

    /**
     * 判断是否是同一实体。此方法只做同一性认定，不代表完全相同。
     * <p>同一性：指<b>相同类型</b>的实体具有相同的id，version。即标识与版本相同</p>
     *
     * @param o 待判断实体
     * @return 是否同一实体
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicEntity<?> that = (BasicEntity<?>) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, getClass());
    }

    @Override
    public String toString() {
        return "BasicEntity{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }

    /**
     * 根据id,获取一个持有该id的指定类型的DTO对象
     *
     * @param id     id
     * @param ignore 用来获取类型, 无实际意义
     * @param <E>    实体类型泛型参数
     * @return 持有指定id的DTO对象
     */
    @SuppressWarnings("unchecked")
    public static <E extends BasicEntity<E>> E refId(Long id, E... ignore) {
        Assert.notNull(ignore, "类型参数不能为null");
        @SuppressWarnings("unchecked")
        E entity = (E) BeanUtils.instantiateClass(ignore.getClass().getComponentType());
        entity.setId(id);
        return entity;
    }
}
