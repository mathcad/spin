package org.spin.core.gson;

import org.spin.core.SpinContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * gson字段排除策略
 * <p>Created by xuweinan on 2016/9/29.</p>
 *
 * @author xuweinan
 */
public class InstantExclusionStrategy implements ExclusionStrategy {
    private Class<?> testCazz;

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        if (!SpinContext.BEAN_FIELDS.containsKey(testCazz.getName())) {
            Map<String, Field> tmp = new HashMap<>();
            Arrays.stream(testCazz.getDeclaredFields()).forEach(fd -> tmp.put(fd.getName(), fd));
            SpinContext.BEAN_FIELDS.put(testCazz.getName(), tmp);
        }
        return SpinContext.BEAN_FIELDS.get(testCazz.getName()).containsKey(f.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

    public void setTestCazz(Class<?> testCazz) {
        this.testCazz = testCazz;
    }
}
