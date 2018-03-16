package org.spin.web;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

/**
 * Restful请求的响应结果
 * <p>Created by xuweinan on 2017/2/19.</p>
 *
 * @author xuweinan
 */
public class RestfulResponse {
    private int code;
    private String message;
    private Object data;

    private RestfulResponse(ErrorCode errorCode) {
        ErrorCode c = errorCode.getCode() >= 400 || errorCode.getCode() == 200 ? errorCode : ErrorCode.INTERNAL_ERROR;
        this.code = c.getCode();
        this.message = c.getDesc();
    }

    private RestfulResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static RestfulResponse ok() {
        return new RestfulResponse(ErrorCode.OK);
    }

    public static RestfulResponse ok(Object data) {
        return new RestfulResponse(200, "OK", data);
    }

    public static RestfulResponse error(SimplifiedException exception) {
        RestfulResponse response = new RestfulResponse(exception.getExceptionType());
        response.setMessage(exception.getSimpleMessage());
        return response;
    }

    public static RestfulResponse error(ErrorCode errorCode) {
        return new RestfulResponse(errorCode);
    }

    public RestfulResponse setCodeAndMsg(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getDesc();
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
