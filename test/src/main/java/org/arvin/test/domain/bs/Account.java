package org.arvin.test.domain.bs;

import org.arvin.test.domain.sys.User;
import org.spin.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2016-10-04 上午8:59
 */
@Entity
@Table(name = "bs_account")
public class Account extends AbstractEntity {

    /**
     * 余额
     */
    @Column(precision = 16, scale = 2)
    private BigDecimal balance = new BigDecimal(0);

    /**
     * 收益
     */
    @Column(precision = 16, scale = 2)
    private BigDecimal income = new BigDecimal(0);

    /**
     * 支付宝名称
     */
    @Column(length = 64)
    private String alipayName;

    /**
     * 支付宝账户
     */
    @Column(length = 64)
    private String alipayAccount;

    /**
     * 微信名称
     */
    @Column(length = 64)
    private String wxName;

    /**
     * 微信账户
     */
    @Column(length = 64)
    private String wxAccount;

    /**
     * 用户
     */
    @OneToOne
    private User user;

    /**
     * 支付密码
     */
    @Column(length = 32)
    private String password;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public String getAlipayName() {
        return alipayName;
    }

    public void setAlipayName(String alipayName) {
        this.alipayName = alipayName;
    }

    public String getAlipayAccount() {
        return alipayAccount;
    }

    public void setAlipayAccount(String alipayAccount) {
        this.alipayAccount = alipayAccount;
    }

    public String getWxName() {
        return wxName;
    }

    public void setWxName(String wxName) {
        this.wxName = wxName;
    }

    public String getWxAccount() {
        return wxAccount;
    }

    public void setWxAccount(String wxAccount) {
        this.wxAccount = wxAccount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Account{" +
                "balance=" + balance +
                ", income=" + income +
                ", alipayName='" + alipayName + '\'' +
                ", alipayAccount='" + alipayAccount + '\'' +
                ", wxName='" + wxName + '\'' +
                ", wxAccount='" + wxAccount + '\'' +
                ", user=" + user +
                ", password='" + password + '\'' +
                '}';
    }
}
