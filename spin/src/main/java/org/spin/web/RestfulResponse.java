package org.spin.web;

import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;

/**
 * Restful请求的相应结果
 * <p>Created by xuweinan on 2017/2/19.
 *
 * @author xuweinan
 */
public class RestfulResponse {
    private int code = -1;
    private String message;
    private Object data;

    private RestfulResponse(ErrorCode errorCode) {
        ErrorCode code = errorCode.getCode() > 400 || errorCode.getCode() == 200 ? errorCode : ErrorCode.INTERNAL_ERROR;
        this.code = code.getCode();
        this.message = code.getDesc();
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
        response.setMessage(exception.getMessage());
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
