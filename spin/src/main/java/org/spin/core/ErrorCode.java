package org.spin.core;

/**
 * 异常与错误代码
 *
 * @author xuweinan
 */
public enum ErrorCode {
    OTHER(-1, "其他"),
    OK(200, "OK"),

    /////////////////////////////////// 内部错误，不应暴露给客户端 ////////////////////////////////////////////////
    DATEFORMAT_UNSUPPORT(5, "时间/日期格式不支持"),
    KEY_FAIL(10, "获取密钥失败"),
    ENCRYPT_FAIL(11, "加密算法执行失败"),
    DEENCRYPT_FAIL(15, "解密算法执行失败"),
    SIGNATURE_FAIL(20, "签名验证失败"),
    BEAN_CREATE_FAIL(40, "创建bean实例错误"),
    IO_FAIL(70, "IO异常"),
    NETWORK_EXCEPTION(100, "网络连接异常"),

    /////////////////////////////////// 可通过Restful接口暴露给客户端的错误 //////////////////////////////////////
    // 4** 访问及权限错误
    LOGGIN_DENINED(400, "登录失败"),
    ACCESS_DENINED(401, "未授权的访问"),
    INVALID_PARAM(412, "参数不合法"),
    NO_BIND_USER(413, "无关联用户"),
    SMS_VALICODE_ERROR(420, "短信验证码错误"),

    // 5** 服务端运行错误
    INTERNAL_ERROR(500, "服务端内部错误"),

    // 6** Token相关错误
    TOKEN_EXPIRED(601, "Token已过期"),
    TOKEN_INVALID(602, "无效的Token"),
    SECRET_EXPIRED(651, "密钥已过期"),
    SECRET_INVALID(653, "无效的密钥");

    private int code;
    private String desc;

    ErrorCode(int value, String desc) {
        this.code = value;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "Exception Type[" + code + "-" + desc + ']';
    }
}
