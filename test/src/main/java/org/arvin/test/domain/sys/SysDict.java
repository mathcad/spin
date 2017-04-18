package org.arvin.test.domain.sys;

import org.spin.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2016-10-04 上午10:32
 */
@Entity
@Table
public class SysDict extends AbstractEntity {

    @Column(length = 64)
    private String dictName;

    @Column(length = 16)
    private String code;

    @Column(length = 64)
    private String dictValue;

    @Column
    private int dictOrder;

    @ManyToOne
    private SysDict parent;

    @Column(length = 128)
    private String remark;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDictValue() {
        return dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public int getDictOrder() {
        return dictOrder;
    }

    public void setDictOrder(int dictOrder) {
        this.dictOrder = dictOrder;
    }

    public SysDict getParent() {
        return parent;
    }

    public void setParent(SysDict parent) {
        this.parent = parent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
