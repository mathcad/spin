package org.spin.cloud.vo;

import java.util.Objects;

/**
 * 角色-权限
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RolePermission {


    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 附加属性
     */
    private String additionalAttr;

    public RolePermission() {
    }

    public RolePermission(String roleCode, String permissionCode) {
        this.roleCode = roleCode;
        this.permissionCode = permissionCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getAdditionalAttr() {
        return additionalAttr;
    }

    public void setAdditionalAttr(String additionalAttr) {
        this.additionalAttr = additionalAttr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermission)) return false;
        RolePermission that = (RolePermission) o;
        return
            roleCode.equals(that.roleCode) &&
                permissionCode.equals(that.permissionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleCode, permissionCode);
    }
}
