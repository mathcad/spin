package org.spin.cloud.web.handler;

import java.util.List;

/**
 * 用户附加验证逻辑
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/8/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface UserVerifier {

    /**
     * 验证用户, 不通过时抛出异常
     *
     * @param uid        用户标识
     * @param target     登录目标
     * @param clientType 客户端类型
     * @param virtual    是否虚拟用户
     * @param userType   业务用户类型
     * @return 用户可用的企业列表
     */
    default List<Long> verify(String uid, String target, Integer clientType, boolean virtual, String userType) {
        return null;
    }
}
