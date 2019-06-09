package org.spin.common.web;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;

/**
 * Restful请求的响应结果
 * <p>Created by xuweinan on 2017/2/19.</p>
 *
 * @author xuweinan
 */
public class RestfulResponse<T> {

    /**
     * 响应时间
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 路径
     */
    private String path;

    /**
     * 状态码
     */
    private int status;

    /**
     * 错误类型
     */
    private String error;

    /**
     * 信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    public RestfulResponse() {
    }

    private RestfulResponse(ErrorCode errorCode) {
        ErrorCode c = errorCode.getCode() >= 400 || errorCode.getCode() == 200 ? errorCode : ErrorCode.INTERNAL_ERROR;
        this.status = c.getCode();
        this.message = c.getDesc();
    }

    private RestfulResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> RestfulResponse<T> ok() {
        return new RestfulResponse<>(ErrorCode.OK);
    }

    public static <T> RestfulResponse<T> ok(T data) {
        return new RestfulResponse<>(200, "OK", data);
    }

    public static <T> RestfulResponse<T> error(SimplifiedException exception) {
        RestfulResponse<T> response = new RestfulResponse<>(exception.getExceptionType());
        response.setMessage(exception.getSimpleMessage());
        return response;
    }

    public static <T> RestfulResponse<T> error(ErrorCode errorCode) {
        return new RestfulResponse<>(errorCode);
    }

    public static <T> RestfulResponse<T> error(ErrorCode errorCode, String message) {
        RestfulResponse<T> response = new RestfulResponse<>(errorCode);
        response.setMessage(message);
        return response;
    }

    public static <T> RestfulResponse<T> error(ErrorCode errorCode, String message, String error) {
        RestfulResponse<T> response = new RestfulResponse<>(errorCode);
        response.setMessage(message);
        response.setError(error);
        return response;
    }

    public RestfulResponse setCodeAndMsg(ErrorCode errorCode) {
        this.status = errorCode.getCode();
        this.message = errorCode.getDesc();
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

