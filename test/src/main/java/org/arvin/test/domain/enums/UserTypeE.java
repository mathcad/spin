package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 用户类型
 * Created by xuweinan on 2016/12/4.
 *
 * @author xuweinan
 */
@UserEnum("用户类型")
public enum UserTypeE {
    平台(1), 普通用户(2), 后台用户(3), 自有用户(4);

    int value;

    UserTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
