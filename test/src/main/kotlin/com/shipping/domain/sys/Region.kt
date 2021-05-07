package com.shipping.domain.sys

import com.shipping.domain.enums.RegionTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 行政区划
 *
 * Created by xuweinan on 2017/04/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_region")
class Region(
    @Column(length = 6, unique = true)
    var code: String = "",

    @Column(length = 64)
    var name: String = "",

    @Column(length = 128)
    var fullName: String = "",

    @Type(type = "org.spin.data.extend.UserEnumType")
    var level: RegionTypeE = RegionTypeE.PROVINCE,

    @Column(length = 6)
    var parentCode: String? = null,

    @Column(length = 64)
    var path: String = ""
) : AbstractEntity<Region>() {
    companion object {
        private const val serialVersionUID = -4148357794686831875L
    }
}
