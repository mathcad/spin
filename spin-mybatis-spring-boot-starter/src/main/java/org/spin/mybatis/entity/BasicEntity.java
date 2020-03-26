package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import org.spin.core.gson.annotation.PreventOverflow;

import java.io.Serializable;

/**
 * 基础实体
 * <p>定义了数据库实体的基本字段, 原则上所有实体均应直接或间接继承{@link BasicEntity}</p>
 * <p>Created by xuweinan on 2019/9/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class BasicEntity implements Serializable {

    /**
     * 主键
     */
    @TableId
    @PreventOverflow
    private Long id;

    /**
     * 数据版本
     */
    @Version
    private Integer version = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
