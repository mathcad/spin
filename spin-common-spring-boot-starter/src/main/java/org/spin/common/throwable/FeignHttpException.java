package org.spin.common.throwable;

import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;

/**
 * Feign客户端异常
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/4/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FeignHttpException extends RuntimeException {

    private int status;
    private String path;
    private String error;
    private String message;

    public FeignHttpException(int status, String path, String error, String message, Throwable cause) {
        super(cause);
        this.status = status;
        this.path = path;
        this.error = error;
        this.message = message;
    }

    public RestfulResponse<Void> toResponse() {
        RestfulResponse<Void> response = RestfulResponse.error(ErrorCode.INTERNAL_ERROR, "远程服务调用失败-[" + message + "]");
        response.setStatus(status);
        response.setPath(path);
        response.setError(error);
        return response;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
