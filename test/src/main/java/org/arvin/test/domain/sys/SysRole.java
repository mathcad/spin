package org.arvin.test.domain.sys;

import org.spin.jpa.core.AbstractEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2016-09-26 下午3:15
 */
@Entity
@Table
public class SysRole extends AbstractEntity {

    @Column(length = 64)
    private String roleName;

    @Column(length = 64)
    private String code;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysFunc> funcList = new ArrayList<>();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<SysFunc> getFuncList() {
        return funcList;
    }

    public void setFuncList(List<SysFunc> funcList) {
        this.funcList = funcList;
    }
}
