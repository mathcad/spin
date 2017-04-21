package com.consultant.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 用户类型
 * Created by xuweinan on 2016/12/4.
 *
 * @author xuweinan
 */
@UserEnum("用户类型")
public enum UserTypeE {
    普通用户(1), 顾问(2);

    int value;

    UserTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
