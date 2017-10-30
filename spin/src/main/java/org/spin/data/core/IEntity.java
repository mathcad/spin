package org.spin.data.core;

import java.io.Serializable;

/**
 * 基础实体接口
 * <p>所有实体均直接或间接实现此接口。只有此接口的实现类才可以被持久化</p>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @param <PK> 主键类型
 * @author xuweinan
 * @version 1.1
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
