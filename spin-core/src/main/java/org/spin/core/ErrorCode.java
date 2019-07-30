package org.spin.core;

import java.io.Serializable;

/**
 * 异常与错误代码
 * <p>自定义代码，请选择800以上的编码，800以内框架保留使用</p>
 * 编码规范：
 * <pre>
 *     -1      其他错误
 *     0-149   内部错误
 *     150-180 SQL错误
 *     4**     访问及权限错误
 *     5**     服务端运行错误
 *     6**     认证相关错误
 * </pre>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 */
public class ErrorCode implements Serializable {
    private static final long serialVersionUID = 2938403856515968992L;

    public static final ErrorCode OTHER = new ErrorCode(-1, "其他");
    public static final ErrorCode OK = new ErrorCode(200, "OK");

    /////////////////////////////////// 内部错误，不应暴露给客户端 ////////////////////////////////////////////////
    public static final ErrorCode DATEFORMAT_UNSUPPORT = new ErrorCode(5, "时间/日期格式不支持");
    public static final ErrorCode KEY_FAIL = new ErrorCode(10, "获取密钥失败");
    public static final ErrorCode ENCRYPT_FAIL = new ErrorCode(11, "加密算法执行失败");
    public static final ErrorCode DEENCRYPT_FAIL = new ErrorCode(15, "解密算法执行失败");
    public static final ErrorCode SIGNATURE_FAIL = new ErrorCode(20, "签名验证失败");
    public static final ErrorCode BEAN_CREATE_FAIL = new ErrorCode(40, "创建bean实例错误");
    public static final ErrorCode IO_FAIL = new ErrorCode(70, "IO异常");
    public static final ErrorCode NETWORK_EXCEPTION = new ErrorCode(100, "网络连接异常");
    public static final ErrorCode SERIALIZE_EXCEPTION = new ErrorCode(120, "JSON序列化错误");

    /////////////////////////////////// 可通过Restful接口暴露给客户端的错误 //////////////////////////////////////
    // 4** 访问及权限错误
    public static final ErrorCode LOGGIN_DENINED = new ErrorCode(400, "登录失败");
    public static final ErrorCode ACCESS_DENINED = new ErrorCode(401, "未授权的访问");
    public static final ErrorCode ASSERT_FAIL = new ErrorCode(410, "数据验证失败");
    public static final ErrorCode INVALID_PARAM = new ErrorCode(412, "参数不合法");
    public static final ErrorCode NO_BIND_USER = new ErrorCode(413, "无关联用户");
    public static final ErrorCode SMS_VALICODE_ERROR = new ErrorCode(420, "短信验证码错误");

    // 5** 服务端运行错误
    public static final ErrorCode INTERNAL_ERROR = new ErrorCode(500, "服务端内部错误");

    // 6** Token相关错误
    public static final ErrorCode TOKEN_EXPIRED = new ErrorCode(601, "Token已过期");
    public static final ErrorCode TOKEN_INVALID = new ErrorCode(602, "无效的Token");
    public static final ErrorCode SECRET_EXPIRED = new ErrorCode(651, "密钥已过期");
    public static final ErrorCode SECRET_INVALID = new ErrorCode(653, "无效的密钥");
    public static final ErrorCode SESSION_INVALID = new ErrorCode(700, "会话已经失效");
    public static final ErrorCode SESSION_EXPIRED = new ErrorCode(702, "会话已经过期");

    private final int code;
    private final String desc;

    public ErrorCode(int value, String desc) {
        this.code = value;
        this.desc = desc;
    }

    public static ErrorCode with(int value, String desc) {
        return new ErrorCode(value, desc);
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "ERROR CODE[" + code + "-" + desc + ']';
    }
}
