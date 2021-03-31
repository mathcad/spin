package org.spin.jpa;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/3/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Condition {
    private boolean ifResult;
    private boolean onElse;
    private boolean elseIfResult;

    public Condition() {
    }

    public Condition(boolean ifResult, boolean onElse, boolean elseIfResult) {
        this.ifResult = ifResult;
        this.onElse = onElse;
        this.elseIfResult = elseIfResult;
    }

    public boolean isIfResult() {
        return ifResult;
    }

    public void setIfResult(boolean ifResult) {
        this.ifResult = ifResult;
    }

    public boolean isOnElse() {
        return onElse;
    }

    public void setOnElse(boolean onElse) {
        this.onElse = onElse;
    }

    public boolean isElseIfResult() {
        return elseIfResult;
    }

    public void setElseIfResult(boolean elseIfResult) {
        this.elseIfResult = elseIfResult;
    }
}
