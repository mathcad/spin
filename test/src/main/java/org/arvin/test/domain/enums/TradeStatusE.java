package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 交易结果
 *
 * @author xuweinan
 */
@UserEnum("交易结果")
public enum TradeStatusE {
    成功(1), 失败(2), 交易中(3);

    int value;

    TradeStatusE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
