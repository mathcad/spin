package org.infrastructure.jpa.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记枚举类型，系统扫描后自动生成
 * 数据字典
 * 
 * @author zhou
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UserEnum {
	String value();
}
