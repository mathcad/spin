package com.shipping.domain.enums

import org.spin.core.annotation.UserEnum
import org.spin.data.core.UserEnumColumn

/**
 * 组织机构类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
enum class OrganizationTypeE(private val value: Int, val desc: String) : UserEnumColumn {
    GROUP(1, "集团"), CONPANY(2, "公司"), SUBSIDIARY(3, "子公司"), BRANCH(4, "分公司"), DIVISION(5, "事业部"), REGION(6, "大区"), DEPARTMENT(7, "部门"), TEAM(8, "团队");

    override fun getValue(): Int {
        return this.value
    }
}
