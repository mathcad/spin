package org.spin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记RestfulApi方法的参数，用来表示该参数是否必须
 * <p>Created by xuweinan on 2016/10/18.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Needed {
}
