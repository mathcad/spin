package org.arvin.test.domain.sys;

import org.arvin.test.domain.enums.RegionTypeE;
import org.hibernate.annotations.Type;
import org.spin.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2016-10-04 上午10:38
 */
@Entity
@Table
public class SysRegion extends AbstractEntity {

    @Column(length = 6)
    private String code;

    @Column(length = 64)
    private String name;

    @Column(length = 128)
    private String fullName;

    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private RegionTypeE level;

    @Column(length = 6)
    private String parentCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public RegionTypeE getLevel() {
        return level;
    }

    public void setLevel(RegionTypeE level) {
        this.level = level;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
