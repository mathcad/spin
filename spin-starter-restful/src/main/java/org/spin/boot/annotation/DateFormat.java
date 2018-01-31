package org.spin.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义Restful方法的时间日期参数的格式
 * <p>Created by xuweinan on 2018/1/31.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DateFormat {

    /**
     * 时间日期格式(LocalDateTime与java.util.Date, java.sql.Date, java.sql.Timestamp类型)
     *
     * @return 格式字符串
     */
    String value() default "yyyy-MM-dd HH:mm:ss";
}
