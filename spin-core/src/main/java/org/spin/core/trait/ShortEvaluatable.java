package org.spin.core.trait;

/**
 * 标识一个特征，表示实现该接口的对象可以求得一个短整型值
 * <p>Created by xuweinan on 2018/1/9.</p>
 *
 * @author xuweinan
 */
public interface ShortEvaluatable extends Evaluatable<Short> {
    short shortValue();

    @Override
    default Short getValue() {
        return shortValue();
    }
}
