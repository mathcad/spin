package com.shipping.domain.enums;

import org.spin.core.annotation.UserEnum;
import org.spin.data.core.UserEnumColumn;

/**
 * 用户类型
 * Created by xuweinan on 2016/12/4.
 *
 * @author xuweinan
 */
@UserEnum("用户类型")
public enum UserTypeE implements UserEnumColumn {
    普通用户(2), 微信用户(3);

    int value;

    UserTypeE(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
