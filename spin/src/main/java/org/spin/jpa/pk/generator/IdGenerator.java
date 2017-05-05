package org.spin.jpa.pk.generator;

import org.spin.jpa.pk.Id;

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
