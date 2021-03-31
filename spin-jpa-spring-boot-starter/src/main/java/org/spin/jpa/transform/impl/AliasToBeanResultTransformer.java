package org.spin.jpa.transform.impl;


import org.spin.core.util.BeanUtils;
import org.spin.jpa.transform.ResultTransformer;
import org.springframework.cglib.beans.BeanMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 别名转Bean结果转换器
 */
public class AliasToBeanResultTransformer implements ResultTransformer {

    private final Class<?> resultClass;

    public AliasToBeanResultTransformer(Class<?> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public Class<?> getResultClass() {
        return resultClass;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Object result;
        result = BeanUtils.instantiateClass(resultClass);
        BeanMap rootBeanMap = BeanMap.create(result);

        Map<String, BeanMap> beanMapMap = new HashMap<>();

        for (int i = 0; i < aliases.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                String[] ap = alias.split("\\.");
                if (ap.length > 1) {
                    BeanMap work = rootBeanMap;
                    for (int j = 0; j < ap.length - 1; j++) {
                        if (!beanMapMap.containsKey(ap[j])) {
                            Class<?> propertyType = (j == 0 ? rootBeanMap : beanMapMap.get(ap[j - 1])).getPropertyType(ap[j]);
                            Object o = BeanUtils.instantiateClass(propertyType);
                            int t = 0;
                            int idx = alias.indexOf('.');
                            while (t++ <= j) {
                                idx = alias.indexOf('.', idx);
                            }
                            String p = alias.substring(0, idx);
                            work.put(p, o);
                            beanMapMap.put(p, BeanMap.create(o));
                        }
                        work = beanMapMap.get(ap[j]);
                    }
                    work.put(ap[ap.length - 1], tuple[i]);
                } else {
                    rootBeanMap.put(alias, tuple[i]);
                }
            }
        }
        return result;
    }

}
