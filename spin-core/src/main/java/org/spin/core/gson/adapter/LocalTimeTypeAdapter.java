package org.spin.core.gson.adapter;

import org.spin.core.gson.DatePatternParser;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalTimeTypeAdapter extends MatchableTypeAdapter<LocalTime> {
    private final DateTimeFormatter formatter;

    public LocalTimeTypeAdapter(String datePattern) {
        this.formatter = DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public LocalTime read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            List<DateTimeFormatter> readPattern = DatePatternParser.getReadPattern(formatter, field);
            DateTimeParseException last = null;
            for (DateTimeFormatter dateTimeFormatter : readPattern) {
                try {
                    return LocalTime.parse(tmp, dateTimeFormatter);
                } catch (DateTimeParseException e) {
                    last = e;
                }
            }

            throw last;
        }
    }

    @Override
    public void write(JsonWriter out, LocalTime value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(DatePatternParser.getWritePattern(formatter, field).format(value));
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return LocalTime.class == type.getRawType();
    }
}
