package org.spin.core.gson.adapter;

import org.spin.core.ErrorCode;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.Evaluatable;
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
        JsonToken jsonToken = in.peek();
        @SuppressWarnings("unchecked")
        Class<E> t = (Class<E>) type.getRawType();
        if (jsonToken == JsonToken.BOOLEAN) {
            try {
                return EnumUtils.getEnum(t, in.nextBoolean());
            } catch (Exception e) {
                throw new SpinException(ErrorCode.SERIALIZE_EXCEPTION, "不支持的枚举转换", e);
            }
        } else {
            String v = in.nextString();
            try {
                return EnumUtils.getEnum(t, v);
            } catch (Exception e) {
                return EnumUtils.fromName(t, v);
            }
        }

    }

    @Override
    public void write(JsonWriter out, E value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            Object enumVal = ((Evaluatable) value).getValue();
            if (enumVal instanceof Number) {
                out.value((Number) enumVal);
            } else if (enumVal instanceof CharSequence) {
                out.value(enumVal.toString());
            } else if (enumVal instanceof Boolean) {
                out.value((Boolean) enumVal);
            }
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        boolean matchIntf = Evaluatable.class.isAssignableFrom(type.getRawType());
        return type.getRawType().isEnum() && matchIntf;
    }
}
