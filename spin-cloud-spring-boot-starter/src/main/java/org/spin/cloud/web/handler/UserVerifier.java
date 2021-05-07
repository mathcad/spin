package org.spin.cloud.web.handler;

import java.io.Serializable;
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

    class UserVerifyInfo implements Serializable {
        private static final long serialVersionUID = 4508858068649633823L;

        /**
         * 唯一标识
         */
        private Long id;

        /**
         * 用户名称
         */
        private String name;

        /**
         * 可用企业列表
         */
        private List<Long> enterprises;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Long> getEnterprises() {
            return enterprises;
        }

        public void setEnterprises(List<Long> enterprises) {
            this.enterprises = enterprises;
        }
    }

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
    default UserVerifyInfo verify(String uid, String target, Integer clientType, boolean virtual, String userType) {
        return null;
    }
}
