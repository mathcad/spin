package org.spin.core.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spin.core.gson.DatePatternParser;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalDateTimeTypeAdapter extends MatchableTypeAdapter<LocalDateTime> {
    private DateTimeFormatter formater;

    public LocalDateTimeTypeAdapter(String datePattern) {
        this.formater = DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public LocalDateTime read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            return LocalDateTime.parse(tmp, DatePatternParser.getReadPattern(formater, field));
        }
    }

    @Override
    public void write(JsonWriter out, LocalDateTime value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(DatePatternParser.getWritePattern(formater, field).format(value));
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return LocalDateTime.class == type.getRawType();
    }
}
