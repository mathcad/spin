package org.spin.jpa;

import java.util.ArrayList;
import java.util.List;

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
    private int elseIfPos = -1;
    private final List<Boolean> elseIfResult = new ArrayList<>();

    public Condition() {
    }

    public Condition(boolean ifResult) {
        this.ifResult = ifResult;
        this.onElse = false;
    }

    public void addElseResult(boolean elseIf) {
        onElse = true;
        elseIfResult.add((-1 == elseIfPos || !getLastElseResult()) && elseIf);
        ++elseIfPos;
    }

    public boolean isIfResult() {
        return ifResult;
    }

    public boolean isOnElse() {
        return onElse;
    }

    public int getElseIfPos() {
        return elseIfPos;
    }

    public List<Boolean> getElseIfResult() {
        return elseIfResult;
    }

    public boolean getLastElseResult() {
        return elseIfResult.get(elseIfPos);
    }
}
