package org.spin.cloud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段映射
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/23</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {
}
