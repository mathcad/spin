package org.spin.core.auth;

import java.util.List;

/**
 * String格式的角色与权限
 * Created by xuweinan on 2017/1/23.
 *
 * @author xuweinan
 */
public class RolePermission {
    /**
     * 用户标识符
     */
    private Object userIdentifier;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 用户标识符
     */
    public Object getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * 用户标识符
     */
    public void setUserIdentifier(Object userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    /**
     * 角色列表
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * 角色列表
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * 权限列表
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * 权限列表
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
