package org.spin.core.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Long类型的GSON适配器
 * <p>Created by xuweinan on 2017/4/25.</p>
 *
 * @author xuweinan
 */
public class LongTypeAdapter extends MatchableTypeAdapter<Long> {

    @Override
    public Long read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            return Long.parseLong(tmp);
        }
    }

    @Override
    public void write(JsonWriter out, Long value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            if (value > 9007199254740992L && null != field && null != field.getAnnotation(PreventOverflow.class)) {
                out.value(value.toString());
            } else {
                out.value(value);
            }
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return Long.class == type.getRawType();
    }
}
