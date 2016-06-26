package org.infrastructure.jpa.core.sqlmap;


import org.infrastructure.jpa.core.annotations.UserEnum;

/**
 * 车辆报价设置
 *
 * @version V1.0
 */
@UserEnum("车辆报价设置")
public enum VhcQuoteTypeE {
    司机报价(0), 调度报价(2), 司机调度都报价(4);

    public int value;

    VhcQuoteTypeE(int value) {
        this.value = value;
    }
}
