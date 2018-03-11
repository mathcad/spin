package org.spin.enhance.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
     * 该类的子类中，该方法无意义，不会被调用。只能返回null且不允许子类重写
     *
     * @param in read in class TypeAdapter
     * @return null
     */
    @Override
    public final T read(JsonReader in) {
        return null;
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
     * 该类的子类中，该方法无意义，不会被调用。什么都不做，且不允许子类重写
     *
     * @param out   输出流
     * @param value 字段值
     */
    @Override
    public final void write(JsonWriter out, T value) {
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
