package org.spin.data.pk.generator;

import org.spin.data.pk.Id;

import java.io.Serializable;

/**
 * 分布式ID生成器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @param <K> ID数据类型
 * @param <I> ID类型
 * @author xuweinan
 * @version 1.0
 */
public interface IdGenerator<K extends Serializable, I extends Id> {

    /**
     * 生成Id
     *
     * @return 主键
     */
    K genId();

    /**
     * 解构Id中的信息
     *
     * @param id 主键
     * @return 主键信息
     */
    I expId(K id);

    /**
     * 获取当前ID生成器生成的ID的类型
     *
     * @return id类型
     */
    Class<K> getIdType();
}
