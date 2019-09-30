package org.spin.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法的作者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Author {

    /**
     * 作者名字
     *
     * @return 名字
     */
    String[] value() default {};

    /**
     * 部门名称
     *
     * @return 部门名称
     */
    String department() default "";

    /**
     * 联系方式
     *
     * @return 联系方式
     */
    String contact() default "";
}
