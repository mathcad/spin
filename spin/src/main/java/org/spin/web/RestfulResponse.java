package org.spin.web;

import org.spin.throwable.SimplifiedException;

/**
 * Created by xuweinan on 2017/2/19.
 *
 * @author xuweinan
 */
public class RestfulResponse {
    private int code = -1;
    private String message;
    private Object data;

    public RestfulResponse() {
    }

    public RestfulResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public RestfulResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static RestfulResponse ok() {
        return new RestfulResponse(200, "OK");
    }

    public static RestfulResponse ok(Object data) {
        return new RestfulResponse(200, "OK", data);
    }

    public static RestfulResponse error(SimplifiedException exception) {
        int code = exception.getExceptionType().getCode() > 400 ? exception.getExceptionType().getCode() : 500;
        return new RestfulResponse(code, exception.getMessage());
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