package org.arvin.test.domain.enums;


import org.spin.annotations.UserEnum;

/**
 * 系统功能类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
public enum FunctionTypeE {
    菜单(1), 按钮(2);

    int value;

    FunctionTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
