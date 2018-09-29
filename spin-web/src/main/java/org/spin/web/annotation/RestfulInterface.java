package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述Restful接口，使用此注解的interface，里面所有标记了RestfulMethod的方法可以被远程调用
 * <p>Created by xuweinan on 2017/9/19.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RestfulInterface {
    String value() default "";
}
