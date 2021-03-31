package org.spin.jpa.transform;

/**
 * 查询结果转换器
 */
public interface ResultTransformer {

    Class<?> getResultClass();

    Object transformTuple(Object[] tuple, String[] aliases);
}
