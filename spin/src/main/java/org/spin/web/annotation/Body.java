package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于注解了RestfulMethod的方法参数中，获取Request中的body内容
 * <p>Created by xuweinan on 2017/12/11.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
}
