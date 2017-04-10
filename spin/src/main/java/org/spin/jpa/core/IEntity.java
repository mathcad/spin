package org.spin.jpa.core;

import java.io.Serializable;

/**
 * 通用实体接口
 * <p>所有实体均直接或间接实现此接口。只有此接口的实现类才可以被持久化</p>
 *
 * @param <PK> 主键类型
 * @author xuweinan
 * @version 1.0
 */
public interface IEntity<PK> extends Serializable {

    /**
     * 获取主键
     */
    PK getId();

    /**
     * 设置主键
     */
    void setId(PK id);
}
