package com.shipping.domain.biz

import org.spin.data.core.AbstractEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * 运单
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午10:29
 */
@Entity
@Table(name = "biz_order")
class Order : AbstractEntity() {

    /**
     * 计划装货时间
     */
    @Column
    var planLoadTime: LocalDateTime? = null

    /**
     * 计划装货时间范围， 单位：天
     */
    @Column(length = 2)
    var planLoadRange: Int = 0

    /**
     * 计划卸货时间
     */
    @Column
    var planUnloadTime: LocalDateTime? = null

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

    /**
     * 收货人姓名
     */
    @Column(length = 64)
    var receiverName: String? = null

    /**
     * 收货人电话
     */
    @Column(length = 64)
    var receiverMobile: String? = null

    /**
     * 收货港
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var receiverPort: Port? = null

    /**
     * 货物名称
     */
    @Column(length = 64)
    var cargoName: String? = null

    /**
     * 货物数量
     */
    @Column(length = 20)
    var cargoQuantity: Float? = null

    /**
     * 货物重量
     */
    @Column(length = 20)
    var cargoWeight: Float? = null

    /**
     * 货物体积
     */
    @Column(length = 20)
    var cargoBulk: Float? = null

    /**
     * 定金
     */
    @Column(length = 20)
    var deposit: Double? = null

    /**
     * 预付款
     */
    @Column(length = 20)
    var prepay: Double? = null

    /**
     * 尾款
     */
    @Column(length = 20)
    var fullpay: Double? = null

    /**
     * 总运费
     */
    @Column(length = 20)
    var sumPay: Double? = null

    override fun toString(): String {
        return "Order{" +
            "planLoadTime=" + planLoadTime +
            ", planLoadRange=" + planLoadRange +
            ", planUnloadTime=" + planUnloadTime +
            ", senderName='" + senderName + '\'' +
            ", senderMobile='" + senderMobile + '\'' +
            ", senderPort=" + senderPort +
            ", receiverName='" + receiverName + '\'' +
            ", receiverMobile='" + receiverMobile + '\'' +
            ", receiverPort=" + receiverPort +
            ", cargoName='" + cargoName + '\'' +
            ", cargoQuantity=" + cargoQuantity +
            ", cargoWeight=" + cargoWeight +
            ", cargoBulk=" + cargoBulk +
            ", deposit=" + deposit +
            ", prepay=" + prepay +
            ", fullpay=" + fullpay +
            ", sumPay=" + sumPay +
            '}'
    }
}
