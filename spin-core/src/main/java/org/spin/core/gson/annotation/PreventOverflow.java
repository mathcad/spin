package org.spin.core.gson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记Long，防止序列化以后，在其他IEEE754标准平台上溢出导致无法正确解析
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PreventOverflow {
}
