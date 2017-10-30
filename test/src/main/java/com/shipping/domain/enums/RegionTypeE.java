package com.shipping.domain.enums;

import org.spin.core.annotation.UserEnum;
import org.spin.data.core.UserEnumColumn;

/**
 * 区域类型
 *
 * @author xuweinan
 */
@UserEnum("区域类型")
public enum RegionTypeE implements UserEnumColumn {
    PROVINCE(1), CITY(2), DISTRICT(3);

    int value;

    RegionTypeE(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
