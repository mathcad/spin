package org.spin.common.db.entity;

import com.baomidou.mybatisplus.core.enums.IEnum;
import org.spin.core.trait.IntegerEvaluatable;

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
public interface MyBatisIntEnum extends IEnum<Integer>, IntegerEvaluatable {

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

    static <T extends MyBatisIntEnum> T valueOf(Class<T> clazz, Integer value) {
        for (T enumConstant : clazz.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        return null;
    }

    static Map<Integer, String> toMap(Class<MyBatisIntEnum> enumClass) {
        MyBatisIntEnum[] enumConstants = enumClass.getEnumConstants();
        Map<Integer, String> res = new HashMap<>(enumConstants.length);
        for (MyBatisIntEnum enumConstant : enumConstants) {
            res.put(enumConstant.getValue(), enumConstant.getDescription());
        }

        return res;
    }
}
