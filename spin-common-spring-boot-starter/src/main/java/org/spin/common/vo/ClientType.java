package org.spin.common.vo;

/**
 * Token申请者的客户端类型
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum ClientType {
    WEB(1, TokenExpireType.FLOAT),
    APP(2, TokenExpireType.FIXED),
    OAUTH(3, TokenExpireType.FIXED);

    private final int value;
    private final TokenExpireType expireType;

    ClientType(int value, TokenExpireType expireType) {
        this.value = value;
        this.expireType = expireType;
    }

    public int getValue() {
        return value;
    }

    public TokenExpireType getExpireType() {
        return expireType;
    }
}
