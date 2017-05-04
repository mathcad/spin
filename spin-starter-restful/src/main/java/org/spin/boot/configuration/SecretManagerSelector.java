package org.spin.boot.configuration;

import org.spin.boot.annotation.EnableSecretManager;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>Created by xuweinan on 2017/5/1.</p>
 *
 * @author xuweinan
 */
public class SecretManagerSelector implements ImportSelector {

    public enum Store {
        /**
         * 内存中存储，重启会丢失。分布式部署或需要持久化保存不应使用该方式
         */
        IN_MENORY,

        /**
         * Redis存储，
         */
        REDIS
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Class<?> annotationType = EnableSecretManager.class;
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
            annotationType.getName(), false));
        Store store = attributes.getEnum("store");
        switch (store) {
            case IN_MENORY:
//                return new String[]{CoreContentConfiguration.class.getName()};
                break;
            case REDIS:
                break;
            default:
                break;
        }
//        if ("core".equals(policy)) {
//            return new String[]{CoreContentConfiguration.class.getName()};
//        } else {
//            return new String[]{SimpleContentConfiguration.class.getName()};
//        }
        return null;
    }
}
