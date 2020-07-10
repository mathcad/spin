package org.spin.core.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 异常工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class ExceptionUtils extends Util {
    private ExceptionUtils() {
    }

    /**
     * 从异常链中解析出指定异常(如果存在的话)，不存在时返回null. 可以正确处理循环引用的情况
     *
     * @param throwable    异常对象
     * @param exceptionCls 需要的异常类型
     * @param <T>          异常泛型参数
     * @return 异常链中指定类型的异常对象
     */
    public static <T extends Throwable> T getCause(Throwable throwable, Class... exceptionCls) {
        Throwable cause = throwable;
        Set<Integer> resolved = new HashSet<>();
        while (null != cause && !isAssignable(cause.getClass(), exceptionCls) && !resolved.contains(cause.hashCode())) {
            resolved.add(cause.hashCode());
            cause = cause.getCause();
        }

        //noinspection unchecked
        return (T) cause;
    }

    public static boolean isAssignable(Class<?> src, Class<?>... targets) {
        boolean match = false;
        if (null != targets) {
            for (Class<?> target : targets) {
                if (target != null && target.isAssignableFrom(src)) {
                    match = true;
                    break;
                }
            }
        }

        return match;
    }
}
