package org.spin.common.throwable;

import org.spin.common.web.RestfulResponse;
import org.spin.core.ErrorCode;

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
    private String error;
    private String message;

    public FeignHttpException(int status, String error, String message, Throwable cause) {
        super(cause);
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public RestfulResponse<Void> toResponse() {
        RestfulResponse<Void> response = RestfulResponse.error(ErrorCode.INTERNAL_ERROR, message);
        response.setStatus(status);
        response.setError(error);
        return response;
    }
}
