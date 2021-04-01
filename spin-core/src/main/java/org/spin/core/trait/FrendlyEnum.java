package org.spin.core.trait;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 业务枚举定义，包含一个value与描述
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface FrendlyEnum<T extends Serializable> extends Evaluatable<T> {

    /**
     * 获取枚举描述
     * <p>默认获取枚举名称</p>
     *
     * @return 枚举描述
     */
    default String getDescription() {
        if (this.getClass().isEnum()) {
            return ((Enum<?>) this).name();
        }
        return "";
    }

    static <E extends Serializable, T extends FrendlyEnum<E>> Optional<T> valueOf(Class<T> clazz, E value) {
        for (T enumConstant : clazz.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return Optional.of(enumConstant);
            }
        }
        return Optional.empty();
    }

    static <E extends Serializable, T extends FrendlyEnum<E>> Map<E, String> toMap(Class<T> enumClass) {
        FrendlyEnum<E>[] enumConstants = enumClass.getEnumConstants();
        Map<E, String> res = new HashMap<>(enumConstants.length);
        for (FrendlyEnum<E> enumConstant : enumConstants) {
            res.put(enumConstant.getValue(), enumConstant.getDescription());
        }

        return res;
    }
}
