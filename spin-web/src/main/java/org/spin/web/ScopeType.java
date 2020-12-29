package org.spin.web;

/**
 * 接口可见范围
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum ScopeType {

    /**
     * 开放
     */
    OPEN("开放"),

    /**
     * 仅内部
     */
    INTERNAL("仅内部"),

    /**
     * 开放，且内部访问无需认证
     */
    OPEN_UNAUTH("开放(内部访问无需认证)");

    private final String desc;

    ScopeType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
