package com.shipping.domain.enums

import org.spin.core.annotation.UserEnum
import org.spin.data.core.UserEnumColumn

/**
 * 系统功能类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
enum class FunctionTypeE(internal var value: Int) : UserEnumColumn {
    MEMU(1), API(2);

    override fun getValue(): Int? {
        return value
    }
}
