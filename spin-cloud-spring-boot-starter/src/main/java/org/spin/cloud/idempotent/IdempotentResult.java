package org.spin.cloud.idempotent;

import java.io.Serializable;

/**
 * 幂等接口结果
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class IdempotentResult implements Serializable {
    private static final long serialVersionUID = 74706151184319494L;

    private String signature;
    private Serializable result;
    private Throwable exception;
    private boolean success;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Serializable getResult() {
        return result;
    }

    public void setResult(Serializable result) {
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
