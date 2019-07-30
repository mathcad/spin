package org.spin.core.util;

import org.spin.core.throwable.SpinException;

/**
 * 异常工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class ExceptionUtils {
    public ExceptionUtils() {
        throw new SpinException("工具类禁止实例化");
    }

    /**
     * 从异常链中解析出指定异常(如果存在的话)，不存在时返回null
     *
     * @param throwable    异常对象
     * @param exceptionCls 需要的异常类型
     * @param <T>          异常泛型参数
     * @return 异常链中指定类型的异常对象
     */
    public static <T extends Throwable> T getCause(Throwable throwable, Class... exceptionCls) {
        Throwable cause = throwable;
        while (null != cause && !isAssignable(cause.getClass(), exceptionCls)) {
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
