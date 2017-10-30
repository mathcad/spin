package com.shipping.domain.biz;

import org.spin.data.core.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 运单
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2017-09-04 下午10:29
 */
@Entity
@Table(name = "biz_order")
public class Order extends AbstractEntity {

    /**
     * 计划装货时间
     */
    @Column
    private LocalDateTime planLoadTime;

    /**
     * 计划装货时间范围， 单位：天
     */
    @Column(length = 2)
    private int planLoadRange;

    /**
     * 计划卸货时间
     */
    @Column
    private LocalDateTime planUnloadTime;

    /**
     * 发货人姓名
     */
    @Column(length = 64)
    private String senderName;

    /**
     * 发货人电话
     */
    @Column(length = 64)
    private String senderMobile;

    /**
     * 发货港
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Port senderPort;

    /**
     * 收货人姓名
     */
    @Column(length = 64)
    private String receiverName;

    /**
     * 收货人电话
     */
    @Column(length = 64)
    private String receiverMobile;

    /**
     * 收货港
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Port receiverPort;

    /**
     * 货物名称
     */
    @Column(length = 64)
    private String cargoName;

    /**
     * 货物数量
     */
    @Column(length = 20)
    private Float cargoQuantity;

    /**
     * 货物重量
     */
    @Column(length = 20)
    private Float cargoWeight;

    /**
     * 货物体积
     */
    @Column(length = 20)
    private Float cargoBulk;

    /**
     * 定金
     */
    @Column(length = 20)
    private Double deposit;

    /**
     * 预付款
     */
    @Column(length = 20)
    private Double prepay;

    /**
     * 尾款
     */
    @Column(length = 20)
    private Double fullpay;

    /**
     * 总运费
     */
    @Column(length = 20)
    private Double sumPay;

    public LocalDateTime getPlanLoadTime() {
        return planLoadTime;
    }

    public void setPlanLoadTime(LocalDateTime planLoadTime) {
        this.planLoadTime = planLoadTime;
    }

    public int getPlanLoadRange() {
        return planLoadRange;
    }

    public void setPlanLoadRange(int planLoadRange) {
        this.planLoadRange = planLoadRange;
    }

    public LocalDateTime getPlanUnloadTime() {
        return planUnloadTime;
    }

    public void setPlanUnloadTime(LocalDateTime planUnloadTime) {
        this.planUnloadTime = planUnloadTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderMobile() {
        return senderMobile;
    }

    public void setSenderMobile(String senderMobile) {
        this.senderMobile = senderMobile;
    }

    public Port getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(Port senderPort) {
        this.senderPort = senderPort;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public Port getReceiverPort() {
        return receiverPort;
    }

    public void setReceiverPort(Port receiverPort) {
        this.receiverPort = receiverPort;
    }

    public String getCargoName() {
        return cargoName;
    }

    public void setCargoName(String cargoName) {
        this.cargoName = cargoName;
    }

    public Float getCargoQuantity() {
        return cargoQuantity;
    }

    public void setCargoQuantity(Float cargoQuantity) {
        this.cargoQuantity = cargoQuantity;
    }

    public Float getCargoWeight() {
        return cargoWeight;
    }

    public void setCargoWeight(Float cargoWeight) {
        this.cargoWeight = cargoWeight;
    }

    public Float getCargoBulk() {
        return cargoBulk;
    }

    public void setCargoBulk(Float cargoBulk) {
        this.cargoBulk = cargoBulk;
    }

    public Double getDeposit() {
        return deposit;
    }

    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }

    public Double getPrepay() {
        return prepay;
    }

    public void setPrepay(Double prepay) {
        this.prepay = prepay;
    }

    public Double getFullpay() {
        return fullpay;
    }

    public void setFullpay(Double fullpay) {
        this.fullpay = fullpay;
    }

    public Double getSumPay() {
        return sumPay;
    }

    public void setSumPay(Double sumPay) {
        this.sumPay = sumPay;
    }

    @Override
    public String toString() {
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
            '}';
    }
}
