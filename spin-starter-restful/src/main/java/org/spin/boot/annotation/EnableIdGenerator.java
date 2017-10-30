package org.spin.boot.annotation;

import org.spin.boot.options.IdGenType;
import org.spin.boot.selectors.IdGeneratorSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否启用主键生成器
 * <p>Created by xuweinan on 2017/9/16.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(IdGeneratorSelector.class)
public @interface EnableIdGenerator {
    IdGenType value() default IdGenType.DISTRIBUTE;
}
