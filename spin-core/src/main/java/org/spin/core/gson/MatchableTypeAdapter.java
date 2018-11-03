package org.spin.core.gson;

import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 提供匹配方法的GSON类型适配器
 * <p>Created by xuweinan on 2017/10/16.</p>
 *
 * @author xuweinan
 */
public abstract class MatchableTypeAdapter<T> extends TypeAdapter<T> {

    /**
     * 定义当前类型适配器适配的类型范围
     *
     * @param type 类型
     * @return 是否匹配
     */
    public abstract boolean isMatch(TypeToken<?> type);

    /**
     * 该类的子类中，该方法无意义，不会被调用。且不允许子类重写
     *
     * @param in read in class TypeAdapter
     * @return null
     */
    @Override
    public final T read(JsonReader in) throws IOException {
        return read(in, null, null);
    }

    /**
     * 反序列化时，通知具体的运行时类型
     *
     * @param in    read in class TypeAdapter
     * @param type  类型
     * @param field 字段本身
     * @return 转换后的java对象，可能为null
     * @throws IOException 当reader读取发生异常时抛出
     */
    public abstract T read(JsonReader in, TypeToken<?> type, Field field) throws IOException;

    /**
     * 该类的子类中，该方法无意义，不会被调用。且不允许子类重写
     *
     * @param out   输出流
     * @param value 字段值
     */
    @Override
    public final void write(JsonWriter out, T value) throws IOException {
        write(out, value, null);
    }

    /**
     * 序列化时，额外通知具体的字段，供实现类使用
     *
     * @param out   输出流
     * @param value 字段值
     * @param field 字段本身
     * @throws IOException 字符流输出异常
     */
    public abstract void write(JsonWriter out, T value, Field field) throws IOException;
}
