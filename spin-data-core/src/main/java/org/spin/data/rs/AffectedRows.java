package org.spin.data.rs;

import org.spin.core.throwable.SimplifiedException;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 受影响行数
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/16</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class AffectedRows {
    private final int num;

    private static final AffectedRows FAIL = new AffectedRows(0);
    private static final AffectedRows SUCC = new AffectedRows(1);

    public static AffectedRows of(int affectedRows) {
        if (1 == affectedRows) {
            return SUCC;
        }

        if (0 == affectedRows) {
            return FAIL;
        }
        return new AffectedRows(affectedRows);
    }

    private AffectedRows(int num) {
        this.num = num;
    }

    public int get() {
        return num;
    }

    public int mustEq(int n, Supplier<String> failMsg) {
        if (num != n) {
            throw new SimplifiedException(failMsg.get());
        }
        return num;
    }

    public int whenFail(Supplier<String> failMsg) {
        if (num <= 0) {
            throw new SimplifiedException(failMsg.get());
        }
        return num;
    }

    public <X extends Exception> int whenFailThrow(Supplier<X> exception) throws X {
        if (num <= 0) {
            throw exception.get();
        }
        return num;
    }

    public int assume(Predicate<Integer> cond, Supplier<String> failMsg) {
        if (!cond.test(num)) {
            throw new SimplifiedException(failMsg.get());
        }

        return num;
    }

    public <X extends Exception> int assumeOrThrow(Predicate<Integer> cond, Supplier<X> exception) throws X {
        if (!cond.test(num)) {
            throw exception.get();
        }
        
        return num;
    }
}
