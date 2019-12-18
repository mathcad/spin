package org.spin.data.pk.converter;


import org.spin.data.pk.Id;

import java.io.Serializable;

/**
 * 分布式ID转换器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @param <K> ID数据类型
 * @param <I> ID类型
 * @author xuweinan
 * @version 1.0
 */
public interface IdConverter<K extends Serializable, I extends Id> {

    /**
     * 将Id对象转换为id
     *
     * @param id id对象
     * @return id
     */
    K convert(I id);

    /**
     * 将id转换为Id对象
     *
     * @param id id
     * @return Id对象
     */
    I convert(K id);
}
