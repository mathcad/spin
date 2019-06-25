package org.spin.common.web;

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
     * 公开
     */
    OPEN,

    /**
     * 仅内部
     */
    INTERNAL,

    /**
     * 公开，且内部访问无需认证
     */
    OPEN_UNAUTH
}
