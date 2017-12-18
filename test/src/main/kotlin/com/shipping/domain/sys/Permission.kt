package com.shipping.domain.sys

import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 *
 * Created by xuweinan on 2017/4/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_permission")
class Permission(

    @Column(length = 64, unique = true)
    var name: String = "",

    @Column(length = 64, unique = true)
    var code: String = "",

    @Column
    var remark: String? = null

) : AbstractEntity() {
    companion object {
        private val serialVersionUID = -724121483134748879L
    }
}
