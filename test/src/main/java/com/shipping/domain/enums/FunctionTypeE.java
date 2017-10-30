package com.shipping.domain.enums;

import org.spin.core.annotation.UserEnum;
import org.spin.data.core.UserEnumColumn;

/**
 * 系统功能类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
public enum FunctionTypeE implements UserEnumColumn {
    MEMU(1), API(2);

    int value;

    FunctionTypeE(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
