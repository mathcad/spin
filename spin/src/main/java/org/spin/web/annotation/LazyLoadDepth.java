package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Created by xuweinan on 2018/3/15.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoadDepth {
    /**
     * 返回hibernate实体时，懒加载对象获取深度
     *
     * @return 深度，默认1
     */
    int value() default 1;

    /**
     * 通过指定索引的参数值来决定加载深度，参数索引从1开始
     *
     * @return 参数位置
     */
    int byArg() default 0;
}
