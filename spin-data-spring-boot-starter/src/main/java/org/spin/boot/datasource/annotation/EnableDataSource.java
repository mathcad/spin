package org.spin.boot.datasource.annotation;

import org.spin.boot.datasource.option.DataSourceType;
import org.spin.boot.datasource.selector.DataSourceBuilderSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否启用数据源自动配置
 * <p>Created by xuweinan on 2017/9/16.</p>
 *
 * @author xuweinan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DataSourceBuilderSelector.class)
public @interface EnableDataSource {
    DataSourceType value() default DataSourceType.DRUID;
}
