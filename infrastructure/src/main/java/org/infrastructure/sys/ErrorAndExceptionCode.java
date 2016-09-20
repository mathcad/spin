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
    KEY_FAIL(10),
    ENCRYPT_FAIL(11),
    DEENCRYPT_FAIL(15),
    BEAN_CREATE_FAIL(40),
    BUSS_EXCETION(60),
    NETWORK_EXCEPTION(100);
    private static final Map<Integer, String> valueStringMapper = new HashMap<>();

    static {
        valueStringMapper.put(-1, "其他");
        valueStringMapper.put(10, "获取密钥失败");
        valueStringMapper.put(11, "加密算法执行失败");
        valueStringMapper.put(15, "解密算法执行失败");
        valueStringMapper.put(40, "创建bean实例错误");
        valueStringMapper.put(60, "业务异常");
        valueStringMapper.put(100, "网络连接异常");
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

    @Override
    public String toString() {
        return "Exception Type[" + this._value + ": " + valueStringMapper.get(this._value) + ']';
    }
}