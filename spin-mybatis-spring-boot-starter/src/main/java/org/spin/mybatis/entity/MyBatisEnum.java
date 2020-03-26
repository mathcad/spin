package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.core.enums.IEnum;
import org.spin.core.trait.Evaluatable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 标记MyBatis的整型枚举属性
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MyBatisEnum<T extends Serializable> extends IEnum<T>, Evaluatable<T> {

    /**
     * 获取枚举描述
     * <p>默认获取枚举名称</p>
     *
     * @return 枚举描述
     */
    default String getDescription() {
        if (this.getClass().isEnum()) {
            return ((Enum) this).name();
        }
        return "";
    }

    static <E extends Serializable, T extends MyBatisEnum<E>> T valueOf(Class<T> clazz, E value) {
        for (T enumConstant : clazz.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        return null;
    }

    static <T extends Serializable> Map<T, String> toMap(Class<MyBatisEnum<T>> enumClass) {
        MyBatisEnum<T>[] enumConstants = enumClass.getEnumConstants();
        Map<T, String> res = new HashMap<>(enumConstants.length);
        for (MyBatisEnum<T> enumConstant : enumConstants) {
            res.put(enumConstant.getValue(), enumConstant.getDescription());
        }

        return res;
    }
}
