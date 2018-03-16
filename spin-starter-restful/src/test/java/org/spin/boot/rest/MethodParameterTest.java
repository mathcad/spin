package org.spin.boot.rest;

import org.junit.jupiter.api.Test;
import org.spin.boot.properties.WxConfigProperties;
import org.spin.core.util.ReflectionUtils;
import org.spin.wx.base.WxConfigInfo;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * <p>Created by xuweinan on 2018/3/16.</p>
 *
 * @author xuweinan
 */
public class MethodParameterTest {

    @Test
    public void test() {
        ReflectionUtils.doWithMethods(MethodParameterTest.class, method -> {
            if (method.getName().equals("a")) {
                Parameter parameter = method.getParameters()[0];
                ParameterizedType type = (ParameterizedType)parameter.getParameterizedType();
                type.getActualTypeArguments();
            }
        });
    }

    public void a(List<WxConfigProperties> a, WxConfigInfo b) {
    }
}
