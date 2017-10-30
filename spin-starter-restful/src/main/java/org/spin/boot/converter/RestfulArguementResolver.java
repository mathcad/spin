package org.spin.boot.converter;

import java.lang.reflect.Type;

/**
 * 自定义Restful请求参数解析
 * <p>Created by xuweinan on 2017/9/27.</p>
 *
 * @author xuweinan
 */
public interface RestfulArguementResolver {

    /**
     * 支持的类型
     */
    Type getSupportedType();

    /**
     * 转换参数
     *
     * @param param 原始参数
     * @return 转换后的参数
     */
    Object convert(Object param);
}
