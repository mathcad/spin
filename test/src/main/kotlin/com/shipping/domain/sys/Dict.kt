package com.shipping.domain.sys

import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * 数据字典
 *
 * Created by xuweinan on 2017/07/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_dict")
class Dict(
    @Column(length = 64)
    var name: String = "",

    @Column(length = 16, unique = true)
    var code: String = "",

    @Column(length = 64)
    var value: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var parent: Dict? = null,

    @Column(length = 128)
    var remark: String? = null
) : AbstractEntity()
