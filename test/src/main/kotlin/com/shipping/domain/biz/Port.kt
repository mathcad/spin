package com.shipping.domain.biz

import kotlinx.serialization.Serializable
import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "biz_port")
@Serializable
class Port : AbstractEntity<Port>() {

    /**
     * 港口名称
     */
    @Column(length = 32)
    var name: String? = null

    /**
     * 经度
     */
    @Column(length = 20)
    var longitude: Double? = null

    /**
     * 纬度
     */
    @Column(length = 20)
    var latitude: Double? = null

    override fun toString(): String {
        return "Port{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}'
    }
}
