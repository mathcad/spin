package com.shipping.domain.sys;

import com.shipping.domain.enums.FunctionTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 功能组
 * <p>Created by xuweinan on 2017/07/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_function_group")
public class FunctionGroup extends AbstractEntity {
    private static final long serialVersionUID = -5089878281368192910L;

    /**
     * 名称
     */
    @Column(length = 64, unique = true)
    private String name;

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    private FunctionTypeE type;

    /**
     * 编码
     */
    @Column(length = 64, unique = true)
    private String code;

    /**
     * 图标
     */
    @Column(length = 64)
    private String icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionTypeE getType() {
        return type;
    }

    public void setType(FunctionTypeE type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
