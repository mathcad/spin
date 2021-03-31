package org.spin.jpa.transform.impl;

import org.spin.jpa.transform.ResultTransformer;

/**
 * 查询结果转换器工厂
 */
final public class Transformers {

    private Transformers() {
    }

    public static final AliasToMapResultTransformer ALIAS_TO_MAP =
        AliasToMapResultTransformer.INSTANCE;


    public static ResultTransformer aliasToBean(Class<?> target) {
        return new AliasToBeanResultTransformer(target);
    }

}
