package org.spin.cloud.vo;

import java.util.Objects;
import java.util.Set;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class OrganVo {
    private long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编号
     */
    private String code;

    /**
     * 机构类型(1-部门, 2-岗位, 3-自定义组织)
     */
    private int type;

    /**
     * 下级机构
     */
    private Set<Long> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<Long> getChildren() {
        return children;
    }

    public void setChildren(Set<Long> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganVo)) return false;
        OrganVo organVo = (OrganVo) o;
        return type == organVo.type &&
            id == organVo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
