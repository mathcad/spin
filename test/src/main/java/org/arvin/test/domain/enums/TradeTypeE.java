package org.arvin.test.domain.enums;

import org.spin.annotations.UserEnum;

/**
 * 交易类型
 *
 * @author xuweinan
 */
@UserEnum("交易类型")
public enum TradeTypeE {
    充值(1), 提现(2), 收益(3), 支出(4), 转入(5), 转出(6);

    int value;

    TradeTypeE(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
