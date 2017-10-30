package org.spin.boot.selectors;

import org.spin.boot.annotation.EnableSecretManager;
import org.spin.boot.configuration.InMemorySecretManagerConfiguration;
import org.spin.boot.options.SecretStorage;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
public class SecretManagerSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Class<?> annotationType = EnableSecretManager.class;
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(annotationType.getName(), false));
        SecretStorage store = attributes.getEnum("value");
        switch (store) {
            case IN_MENORY:
                return new String[]{InMemorySecretManagerConfiguration.class.getName()};
            case REDIS:
                throw new UnsupportedOperationException("Redis存储的SecretDao尚未实现");
            default:
                throw new UnsupportedOperationException("不支持的Secret存储方式");
        }
    }
}
