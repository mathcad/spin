package org.infrastructure.sys;

import java.util.HashMap;
import java.util.Map;

/**
 * 异常与错误代码
 *
 * @author xuweinan
 */
public enum ErrorAndExceptionCode {
    OTHER(-1),
    ENCRYPT_FAIL(10),
    DEENCRYPT_FAIL(15),
    BEAN_CREATE_FAIL(40);
    private static final Map<Integer, String> valueStringMapper = new HashMap<Integer, String>();

    static {
        valueStringMapper.put(-1, "其他");
        valueStringMapper.put(10, "加密算法执行错误");
        valueStringMapper.put(15, "解密算法执行错误");
        valueStringMapper.put(40, "创建bean实例错误");
    }

    private int _value;

    ErrorAndExceptionCode(int value) {
        this._value = value;
    }

    public int getValue() {
        return this._value;
    }

    public String getDesc() {
        return valueStringMapper.get(this._value);
    }
}
