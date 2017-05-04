package org.spin.boot.annotation;

import org.spin.boot.configuration.SecretManagerSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否启用Token、Key的管理容器
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SecretManagerSelector.class)
public @interface EnableSecretManager {
    SecretManagerSelector.Store store() default SecretManagerSelector.Store.IN_MENORY;
}
