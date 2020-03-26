package org.spin.cloud.vo;

import org.spin.cloud.throwable.BizException;

/**
 * Token过期方式
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum TokenExpireType {

    /**
     * 固定期限策略
     */
    FIXED(1),

    /**
     * 滑动时间窗口策略
     */
    FLOAT(2);

    private final int value;

    TokenExpireType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TokenExpireType getByValue(String value) {
        if ("1".equals(value)) {
            return FIXED;
        } else if ("2".equals(value)) {
            return FLOAT;
        } else {
            throw new BizException("不合法的Token类型");
        }
    }
}
