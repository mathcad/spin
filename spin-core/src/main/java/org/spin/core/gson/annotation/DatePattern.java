package org.spin.core.gson.annotation;

import org.spin.core.util.JsonUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 定义类的时间日期类型成员变量在json序列化、反序列化时遵循的格式
 * <p>
 * <strong>配合{@link JsonUtils}使用，仅支持java8中新的时间日期类型({@link LocalDateTime}, {@link LocalDate}, {@link LocalTime})</strong>
 * </p>
 * <p>默认为空，采用JsonUtils的全局设置。如果只指定其中一个格式，无论序列化、反序列化都会采用该格式</p>
 * <p>Created by xuweinan on 2018/3/10.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface DatePattern {
    /**
     * 指定读取时的时间日期模式
     * <p>默认为空，采用全局设置</p>
     *
     * @return 日期格式
     */
    String read() default "";

    /**
     * 指定写出时的时间日期模式
     * <p>默认为空，采用全局设置</p>
     *
     * @return 日期格式
     */
    String write() default "";
}
