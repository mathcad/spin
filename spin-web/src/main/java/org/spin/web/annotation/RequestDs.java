package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestDs {

    /**
     * 数据源名称
     *
     * @return 数据源名称
     */
    String value() default "";

    /**
     * 是否自动开启Session
     *
     * @return 是否自动开启Session
     */
    boolean openSession() default true;
}
