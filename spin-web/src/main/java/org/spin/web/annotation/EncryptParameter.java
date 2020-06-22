package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记Controller的加密参数
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptParameter {
}
