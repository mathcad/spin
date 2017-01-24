package org.spin.shiro;

import java.util.List;

/**
 * String格式的角色与权限
 * Created by xuweinan on 2017/1/23.
 *
 * @author xuweinan
 */
public class RolePermission {
    private List<String> roles;
    private List<String> permissions;

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}