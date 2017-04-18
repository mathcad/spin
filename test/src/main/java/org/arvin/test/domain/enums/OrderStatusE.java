package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * <描述>
 *
 * @author X
 * @contact TEL:18900539326, QQ:396616781
 * @create 2016-10-06 下午9:45
 */
@UserEnum("订单支付状态")
public enum OrderStatusE {

    未支付(1), 已支付(2), 支付中(3);

    int value;

    OrderStatusE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
