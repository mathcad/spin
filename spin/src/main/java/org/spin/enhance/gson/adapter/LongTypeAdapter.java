package org.spin.enhance.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.core.util.StringUtils;
import org.spin.enhance.gson.MatchableTypeAdapter;
import org.spin.enhance.gson.annotation.PreventOverflow;

import java.io.IOException;

/**
 * Long类型的GSON适配器
 * <p>Created by xuweinan on 2017/4/25.</p>
 *
 * @author xuweinan
 */
public class LongTypeAdapter extends MatchableTypeAdapter<Long> {
    @Override
    public void write(JsonWriter out, Long value) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            if (value > 9007199254740992L)
                out.value(value.toString());
            else
                out.value(value);
        }
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            return Long.parseLong(in.nextString());
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return Long.class.isAssignableFrom(type.getRawType()) && type.getRawType().getAnnotation(PreventOverflow.class) != null;
    }
}
