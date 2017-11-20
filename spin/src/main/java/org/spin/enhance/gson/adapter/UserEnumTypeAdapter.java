package org.spin.enhance.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.core.trait.IntEvaluated;
import org.spin.core.util.EnumUtils;
import org.spin.data.core.UserEnumColumn;
import org.spin.enhance.gson.MatchableTypeAdapter;

import java.io.IOException;

/**
 * GSON的枚举适配器
 * Created by xuweinan on 2017/1/25.
 */
public class UserEnumTypeAdapter<E extends Enum<E>> extends MatchableTypeAdapter<E> {
    private ThreadLocal<Class<E>> clazz;

    public UserEnumTypeAdapter() {
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(((UserEnumColumn) value).getValue());
        }
    }

    @Override
    public E read(JsonReader in) throws IOException {
        try {
            return EnumUtils.getEnum(clazz.get(), in.nextInt());
        } catch (IllegalStateException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        boolean matchIntf = false;
        Class<?>[] intfs = type.getRawType().getInterfaces();
        for (Class<?> intf : intfs) {
            matchIntf = UserEnumColumn.class.getName().equals(intf.getName());
            if (matchIntf) {
                break;
            }
        }
        boolean match = Enum.class.isAssignableFrom(type.getRawType()) && matchIntf;
        if (match) {
            //noinspection unchecked
            clazz.set((Class<E>) type.getRawType());
        }
        return match;
    }
}
