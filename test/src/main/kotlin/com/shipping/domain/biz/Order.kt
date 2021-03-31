package com.shipping.domain.biz

import kotlinx.serialization.Serializable
import org.spin.data.core.AbstractEntity
import javax.persistence.*

@Entity
@Table(name = "biz_order")
@Serializable
class Order : AbstractEntity<Order>() {


    /**
     * 发货人姓名
     */
    @Column(length = 64)
    var senderName: String? = null

    /**
     * 发货人电话
     */
    @Column(length = 64)
    var senderMobile: String? = null

    /**
     * 发货港
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var senderPort: Port? = null


    override fun toString(): String {
        return "Order{" +
                ", senderName='" + senderName + '\'' +
                ", senderMobile='" + senderMobile + '\'' +
                ", senderPort=" + senderPort +
                '}'
    }
}
