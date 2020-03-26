package org.spin.boot.datasource.selector;

import org.spin.boot.datasource.annotation.EnableDataSource;
import org.spin.boot.datasource.configuration.DataSourceAutoConfiguration;
import org.spin.boot.datasource.configuration.DruidDataSourceBuilder;
import org.spin.boot.datasource.configuration.HikariDataSourceBuilder;
import org.spin.boot.datasource.configuration.OpenSessionInViewConfiguration;
import org.spin.boot.datasource.option.DataSourceType;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
public class DataSourceBuilderSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Class<?> annotationType = EnableDataSource.class;
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(annotationType.getName(), false));
        DataSourceType type = attributes.getEnum("value");
        switch (type) {
            case DRUID:
                return new String[]{DruidDataSourceBuilder.class.getName(),
                    DataSourceAutoConfiguration.class.getName(),
                    OpenSessionInViewConfiguration.class.getName()
                };
            case HIKARICP:
                return new String[]{HikariDataSourceBuilder.class.getName(),
                    DataSourceAutoConfiguration.class.getName(),
                    OpenSessionInViewConfiguration.class.getName()
                };
        }
        throw new UnsupportedOperationException("不支持的数据源类型");
    }
}
