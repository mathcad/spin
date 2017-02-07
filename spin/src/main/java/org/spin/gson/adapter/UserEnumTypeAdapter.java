package org.spin.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.util.EnumUtils;

import java.io.IOException;

/**
 * Created by Arvin on 2017/1/25.
 */
public class UserEnumTypeAdapter<E extends Enum<E>> extends TypeAdapter<E> {
    private Class<E> clazz;

    public UserEnumTypeAdapter(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        out.value(EnumUtils.getEnumValue(value.getDeclaringClass(), value));
    }

    @Override
    public E read(JsonReader in) throws IOException {
        return EnumUtils.getEnum(clazz, in.nextInt());
    }
}