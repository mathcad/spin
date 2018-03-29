package com.shipping.domain.sys

import com.shipping.domain.enums.UserTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractUser
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * 系统用户信息
 */
@Entity
@Table(name = "sys_user", indexes = [(Index(columnList = "mobile")), (Index(columnList = "openId"))])
class User(
    @Column(length = 64)
    var nickname: String? = null,

    @Column(length = 32)
    var realName: String? = null,

    @ManyToOne
    var headImg: File? = null,

    @Column(length = 64)
    var email: String? = null,

    @Column(length = 14, unique = true)
    var mobile: String? = null,

    @Column(length = 64)
    var openId: String? = null,

    @Type(type = "org.spin.data.extend.UserEnumType")
    var userType: UserTypeE? = null,

    @Column(length = 128)
    var about: String? = null,

    /**
     * 登记机构
     */
    @ManyToOne
    var organ: Organization? = null,

    /**
     * 所属机构
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_organ", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "organ_id")])
    var organs: MutableList<Organization> = ArrayList(),

    /**
     * 拥有角色
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "role_id")])
    var roles: MutableList<Role> = ArrayList(),

    /**
     * 拥有权限
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_permission", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "permission_id")])
    var permissions: MutableList<Permission> = ArrayList()
) : AbstractUser() {
    companion object {
        private const val serialVersionUID = -7875157725232505055L
    }
}
