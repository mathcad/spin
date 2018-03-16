package org.spin.data.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 数据字典
 * <p>Created by xuweinan on 2017/07/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_dict")
public class Dict extends AbstractEntity {
    private static final long serialVersionUID = 8647552774673463154L;

    @Column(length = 64)
    private String name;

    @Column(length = 16, unique = true)
    private String code;

    @Column(length = 64)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    private Dict parent;

    @Column(length = 128)
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Dict getParent() {
        return parent;
    }

    public void setParent(Dict parent) {
        this.parent = parent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
