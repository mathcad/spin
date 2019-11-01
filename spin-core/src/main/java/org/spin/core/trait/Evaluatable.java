package org.spin.core.trait;

/**
 * 标识一个特征，表示实现该接口的对象可以求得一个指定类型的值
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface Evaluatable<T> {
    T getValue();
}
