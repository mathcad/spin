package com.consultant.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 区域类型
 *
 * @author xuweinan
 */
@UserEnum("区域类型")
public enum RegionTypeE {
    PROVINCE(1), CITY(2), DISTRICT(3);

    int value;

    RegionTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
