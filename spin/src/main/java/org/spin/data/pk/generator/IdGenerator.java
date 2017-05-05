package org.spin.data.pk.generator;

import org.spin.data.pk.Id;

import java.io.Serializable;


public interface IdGenerator<K extends Serializable,I extends Id> {

    /**
     * 生成Id
     */
    K genId();

    /**
     * 解构Id中的信息
     */
    I expId(K id);
}
