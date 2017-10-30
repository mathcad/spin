package org.spin.boot.selectors;

import org.spin.boot.annotation.EnableIdGenerator;
import org.spin.boot.configuration.DistributeIdGenConfiguration;
import org.spin.boot.options.IdGenType;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
public class IdGeneratorSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Class<?> annotationType = EnableIdGenerator.class;
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(annotationType.getName(), false));
        IdGenType type = attributes.getEnum("value");
        switch (type) {
            case DISTRIBUTE:
                return new String[]{DistributeIdGenConfiguration.class.getName()};
            default:
                throw new UnsupportedOperationException("不支持的主键生成策略");
        }
    }
}
