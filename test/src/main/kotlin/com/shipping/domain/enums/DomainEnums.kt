package com.shipping.domain.enums

import org.spin.core.annotation.UserEnum
import org.spin.data.core.UserEnumColumn

/**
 * 系统功能类型
 *
 * @author xuweinan
 */
@UserEnum("系统功能类型")
enum class FunctionTypeE(private val value: Int, val desc: String) : UserEnumColumn {
    ELEMENT(0, "页面元素"), MEMU(1, "菜单"), API(2, "接口");

    override fun getValue(): Int {
        return value
    }
}

/**
 * 组织机构类型
 *
 * @author xuweinan
 */
@UserEnum("组织机构类型")
enum class OrganizationTypeE(private val value: Int, val desc: String) : UserEnumColumn {
    GROUP(1, "集团"), CONPANY(2, "公司"), SUBSIDIARY(3, "子公司"), BRANCH(4, "分公司"), DIVISION(5, "事业部"), REGION(6, "大区"), DEPARTMENT(7, "部门"), TEAM(8, "团队");

    override fun getValue(): Int {
        return this.value
    }
}

/**
 * 区域类型
 *
 * @author xuweinan
 */
@UserEnum("区域类型")
enum class RegionTypeE(private val value: Int, val desc: String) : UserEnumColumn {
    PROVINCE(1, "省"), CITY(2, "市"), DISTRICT(3, "区/县"), TOWN(4, "街道");

    override fun getValue(): Int {
        return value
    }
}

/**
 * 用户类型
 * Created by xuweinan on 2016/12/4.
 *
 * @author xuweinan
 */
@UserEnum("用户类型")
enum class UserTypeE(private val value: Int) : UserEnumColumn {
    普通用户(2), 微信用户(3);

    override fun getValue(): Int {
        return value
    }
}
