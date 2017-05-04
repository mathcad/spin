package com.consultant.domain.enums;


import org.spin.core.annotation.UserEnum;

/**
 * 系统功能类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
public enum FunctionTypeE {
    菜单(1), 接口(2);

    int value;

    FunctionTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
