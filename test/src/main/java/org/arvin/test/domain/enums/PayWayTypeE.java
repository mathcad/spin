package org.arvin.test.domain.enums;


import org.spin.annotations.UserEnum;

/**
 * 支付路径
 *
 * @author xuweinan
 */
@UserEnum("支付路径")
public enum PayWayTypeE {
    微信(1), 支付宝(2), 内部账户(3), 线下(4);

    int value;

    PayWayTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
