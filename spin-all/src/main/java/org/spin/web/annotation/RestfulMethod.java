package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestfulMethod {

    /**
     * 是否需要认证，默认true
     *
     * @return 是否需要认证
     */
    boolean auth() default true;

    /**
     * 名称，默认为方法名(资源访问路径)
     *
     * @return 资源访问路径
     */
    String value() default "";

    /**
     * 权限路径
     *
     * @return 权限定义字符串
     */
    String authRouter() default "";

    /**
     * 返回hibernate实体时，懒加载对象获取深度
     * <p>指定深度小于0时，不做处理</p>
     *
     * @return 深度，默认1
     */
    int fetchDepth() default 1;
}
