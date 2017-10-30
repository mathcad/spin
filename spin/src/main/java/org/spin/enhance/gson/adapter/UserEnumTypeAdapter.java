package org.spin.enhance.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.core.trait.IntEvaluated;
import org.spin.core.util.EnumUtils;
import org.spin.enhance.gson.MatchableTypeAdapter;

import java.io.IOException;

/**
 * GSON的枚举适配器
 * Created by xuweinan on 2017/1/25.
 */
public class UserEnumTypeAdapter<E extends Enum<E>> extends MatchableTypeAdapter<E> {
    private Class<E> clazz;

    public UserEnumTypeAdapter(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value((Integer) EnumUtils.getEnumValue(value.getDeclaringClass(), value));
        }
    }

    @Override
    public E read(JsonReader in) throws IOException {
        try {
            return EnumUtils.getEnum(clazz, in.nextInt());
        } catch (IllegalStateException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        boolean matchIntf = false;
        Class<?>[] intfs = type.getRawType().getInterfaces();
        for (Class<?> intf : intfs) {
            matchIntf = IntEvaluated.class.getName().equals(intf.getName());
            if (matchIntf) {
                break;
            }
        }
        return Enum.class.isAssignableFrom(type.getRawType()) && matchIntf;
    }
}
