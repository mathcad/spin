package org.spin.core.trait;

/**
 * 标识一个特征，表示实现该接口的对象可以求得一个字节值
 * <p>Created by xuweinan on 2018/1/9.</p>
 *
 * @author xuweinan
 */
public interface ByteEvaluatable extends Evaluatable<Byte> {
    byte byteValue();

    @Override
    default Byte getValue() {
        return byteValue();
    }
}
