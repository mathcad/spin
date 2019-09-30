package org.spin.common.web;

/**
 * 认证级别
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum AuthLevel {

    /**
     * 无需认证
     */
    NONE("无需认证"),

    /**
     * 仅认证
     */
    AUTHENCATE("仅认证"),

    /**
     * 授权访问
     */
    AUTHORIZE("授权访问");

    private final String desc;

    AuthLevel(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
