package org.spin.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记枚举类型，系统扫描后自动生成数据字典
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UserEnum {

    /**
     * 枚举名称
     *
     * @return 枚举名称
     */
    String value();
}
