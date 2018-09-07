package org.spin.core.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.trait.IntEvaluatable;
import org.spin.core.util.EnumUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * GSON的枚举适配器
 * Created by xuweinan on 2017/1/25.
 */
public class UserEnumTypeAdapter<E extends Enum<E>> extends MatchableTypeAdapter<E> {

    @Override
    public E read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String v = in.nextString();
        @SuppressWarnings("unchecked")
        Class<E> t = (Class<E>) type.getRawType();
        try {
            Integer iv = Integer.valueOf(v);
            return EnumUtils.getEnum(t, iv);
        } catch (Exception e) {
            return EnumUtils.fromName(t, v);
        }
    }

    @Override
    public void write(JsonWriter out, E value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(((IntEvaluatable) value).getValue());
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        boolean matchIntf = IntEvaluatable.class.isAssignableFrom(type.getRawType());
        return Enum.class.isAssignableFrom(type.getRawType()) && matchIntf;
    }
}
