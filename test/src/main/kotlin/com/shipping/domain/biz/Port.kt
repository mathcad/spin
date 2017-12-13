package com.shipping.domain.biz

import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 港口
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午10:43
 */
@Entity
@Table(name = "biz_port")
class Port : AbstractEntity() {

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
