package org.arvin.test.domain.bs;

import org.arvin.test.domain.enums.PayWayTypeE;
import org.arvin.test.domain.enums.TradeStatusE;
import org.arvin.test.domain.enums.TradeTypeE;
import org.hibernate.annotations.Type;
import org.spin.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bs_account_detail")
public class AccountDetail extends AbstractEntity {

    /**
     * 操作类型
     */
    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private TradeTypeE type;

    /**
     * 广告主题 或者 操作标题
     */
    @Column(length = 64)
    private String title;

    /**
     * 操作金额
     */
    @Column(precision = 16, scale = 2)
    private BigDecimal money;

    /**
     * 每次操作余额
     */
    @Column(precision = 16, scale = 2)
    private BigDecimal balance;

    /**
     * 操作时间
     */
    @Column
    private LocalDateTime operTime;

    /**
     * 源账户
     */
    @ManyToOne
    private Account fromAccount;

    /**
     * 目标账户
     */
    @ManyToOne
    private Account toAccount;

    /**
     * 1：成功 2：失败 3：交易中
     */
    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private TradeStatusE status;

    /**
     * 支付路径 1：支付宝 2：微信
     */
    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private PayWayTypeE payWay;

    /**
     * 支付账户名
     */
    @Column(length = 64)
    private String payName;

    /**
     * 支付账号
     */
    @Column(length = 64)
    private String payAccount;

    @Column(length = 256)
    private String remark;

    public TradeTypeE getType() {
        return type;
    }

    public void setType(TradeTypeE type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public TradeStatusE getStatus() {
        return status;
    }

    public void setStatus(TradeStatusE status) {
        this.status = status;
    }

    public PayWayTypeE getPayWay() {
        return payWay;
    }

    public void setPayWay(PayWayTypeE payWay) {
        this.payWay = payWay;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public String getPayAccount() {
        return payAccount;
    }

    public void setPayAccount(String payAccount) {
        this.payAccount = payAccount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
