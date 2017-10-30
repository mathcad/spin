package com.shipping.domain.sys;

import com.shipping.domain.enums.RegionTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 行政区划
 * <p>Created by xuweinan on 2017/04/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_region")
public class Region extends AbstractEntity {
    private static final long serialVersionUID = -4148357794686831875L;

    @Column(length = 6, unique = true)
    private String code;

    @Column(length = 64)
    private String name;

    @Column(length = 128)
    private String fullName;

    @Type(type = "org.spin.data.extend.UserEnumType")
    private RegionTypeE level;

    @Column(length = 6)
    private String parentCode;

    @Column(length = 64)
    private String path;

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
