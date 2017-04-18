package org.arvin.test.domain.sys;

import org.arvin.test.domain.enums.FunctionTypeE;
import org.hibernate.annotations.Type;
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
 * @create 2016-10-04 上午10:23
 */
@Entity
@Table
public class SysFunc extends AbstractEntity {

    /**
     * 功能菜单名称
     */
    @Column(length = 64)
    private String funcName;

    /**
     * 类型 0:菜单 1按钮
     */
    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private FunctionTypeE type;

    /**
     * 编码
     */
    @Column(length = 32)
    private String code;

    /**
     * 排序
     */
    @Column
    private int funcOrder;

    /**
     * 连接
     */
    @Column(length = 128)
    private String url;

    @ManyToOne
    private SysFunc parent;

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
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

    public int getFuncOrder() {
        return funcOrder;
    }

    public void setFuncOrder(int funcOrder) {
        this.funcOrder = funcOrder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SysFunc getParent() {
        return parent;
    }

    public void setParent(SysFunc parent) {
        this.parent = parent;
    }
}
