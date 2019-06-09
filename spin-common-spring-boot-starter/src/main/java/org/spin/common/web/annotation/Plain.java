package org.spin.common.web.annotation;

import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解标注的Controller或者方法，将不会包装返回结果
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface Plain {
}
