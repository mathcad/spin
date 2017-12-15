package com.shipping.domain.enums

import org.spin.core.annotation.UserEnum
import org.spin.data.core.UserEnumColumn

/**
 * 区域类型
 *
 * @author xuweinan
 */
@UserEnum("区域类型")
enum class RegionTypeE(private val value: Int) : UserEnumColumn {
    PROVINCE(1), CITY(2), DISTRICT(3);

    override fun getValue(): Int {
        return value
    }
}
