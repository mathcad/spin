package org.spin.data.selector;

import org.spin.data.annotation.EnableIdGenerator;
import org.spin.data.configuration.DistributeIdGenConfiguration;
import org.spin.data.option.IdGenType;
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
        if (type == IdGenType.DISTRIBUTE) {
            return new String[]{DistributeIdGenConfiguration.class.getName()};
        }
        throw new UnsupportedOperationException("不支持的主键生成策略");
    }
}
