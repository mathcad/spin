package org.spin.data.pk.generator;

import org.spin.data.pk.Id;

import java.io.Serializable;


public interface IdGenerator<K extends Serializable, I extends Id> {

    /**
     * 生成Id
     */
    K genId();

    /**
     * 解构Id中的信息
     */
    I expId(K id);

    /**
     * 获取当前ID生成器生成的ID的类型
     *
     * @return id类型
     */
    Class<K> getIdType();
}
