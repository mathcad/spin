package org.spin.enhance.gson;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

/**
 * 非确定类型的类型适配器
 * <p>Created by xuweinan on 2018/3/1.</p>
 *
 * @author xuweinan
 */
public abstract class UncertaintyMatchableTypeAdapter<T> extends MatchableTypeAdapter<T> {

    /**
     * 针对非确定类型的反序列化，通知具体的运行时类型
     *
     * @param in   read in class TypeAdapter
     * @param type 类型
     * @return 转换后的java对象，可能为null
     * @throws IOException 当reader读取发生异常时抛出
     */
    public abstract T read(JsonReader in, TypeToken<?> type) throws IOException;

    /**
     * 非确定类型时，不指定具体类型的反序列化无意义，只能返回null且不允许子类重写
     *
     * @param in read in class TypeAdapter
     * @return null
     */
    @Override
    public final T read(JsonReader in) {
        return null;
    }
}
