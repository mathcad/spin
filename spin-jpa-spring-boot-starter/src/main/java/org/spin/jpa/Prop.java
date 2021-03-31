package org.spin.jpa;

import org.spin.core.function.serializable.Function;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.LambdaUtils;

import java.io.Serializable;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/3/29</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface Prop<T, R> extends Function<T, R>, Serializable {

    static <T, R1, R2> PropImpl<T> path(Function<T, R1> f1, Function<R1, R2> f2) {
        return t -> BeanUtils.toFieldName(LambdaUtils.resolveLambda(f1).getImplMethodName()) + "." + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f2).getImplMethodName());
    }

    static <T, R1, R2, R3> PropImpl<T> path(Function<T, R1> f1, Function<R1, R2> f2, Function<R2, R3> f3) {
        return t -> BeanUtils.toFieldName(LambdaUtils.resolveLambda(f1).getImplMethodName()) + "."
            + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f2).getImplMethodName()) + "."
            + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f3).getImplMethodName());
    }

    static <T, R1, R2, R3, R4> PropImpl<T> path(Function<T, R1> f1, Function<R1, R2> f2, Function<R2, R3> f3, Function<R3, R4> f4) {
        return t -> BeanUtils.toFieldName(LambdaUtils.resolveLambda(f1).getImplMethodName()) + "."
            + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f2).getImplMethodName()) + "."
            + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f3).getImplMethodName()) + "."
            + BeanUtils.toFieldName(LambdaUtils.resolveLambda(f4).getImplMethodName());
    }
}
