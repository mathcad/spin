package com.shipping.domain.sys

import org.spin.data.core.AbstractEntity
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

/**
 * 角色
 *
 * Created by xuweinan on 2017/04/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_role")
class Role(
    @Column(length = 64, unique = true)
    var name: String = "",

    @Column(length = 64, unique = true)
    var code: String = "",

    @Column
    var remark: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_role_permission", joinColumns = [JoinColumn(name = "role_id")], inverseJoinColumns = [JoinColumn(name = "permission_id")])
    var permissions: MutableList<Permission> = ArrayList()
) : AbstractEntity() {
    companion object {
        private const val serialVersionUID = 8934787783435264166L
    }
}
