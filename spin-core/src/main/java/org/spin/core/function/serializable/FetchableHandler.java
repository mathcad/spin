package org.spin.core.function.serializable;

import java.io.Serializable;

/**
 * 无参数，有返回结果的handler
 * <p>Created by xuweinan on 2017/10/27.</p>
 *
 * @author xuweinan
 */
@FunctionalInterface
public interface FetchableHandler<T> extends org.spin.core.function.FetchableHandler<T>, Serializable {
}
