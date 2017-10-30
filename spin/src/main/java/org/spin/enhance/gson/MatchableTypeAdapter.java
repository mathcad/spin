package org.spin.enhance.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

/**
 * 提供匹配方法的GSON类型适配器
 * <p>Created by xuweinan on 2017/10/16.</p>
 *
 * @author xuweinan
 */
public abstract class MatchableTypeAdapter<T> extends TypeAdapter<T> {

    /**
     * 当前类型适配器是否匹配指定类型
     *
     * @param type 类型
     * @return 是否匹配
     */
    public abstract boolean isMatch(TypeToken<?> type);
}
