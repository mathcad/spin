package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.spin.data.pk.Id;
import org.spin.data.pk.generator.IdGenerator;

/**
 * Mybatis Plus的ID生成器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SfIdGenerator implements IdentifierGenerator {

    private final IdGenerator<? extends Number, ? extends Id> idGenerator;

    public SfIdGenerator(IdGenerator<? extends Number, ? extends Id> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Number nextId(Object entity) {
        return idGenerator.genId();
    }
}
