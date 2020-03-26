package org.spin.cloud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个工具类
 * <p>被该注解标记的工具类, 允许指定一个初始化方法，在系统启动时系统会自动调用该初始化方法(允许依赖Spring中的bean)</p>
 * <p>Created by xuweinan on 2019/9/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UtilClass {

    /**
     * 初始化方法名称, 默认为init
     *
     * @return 初始化方法名称
     */
    String initMethod() default "init";
}
