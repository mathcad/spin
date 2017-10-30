package org.spin.enhance.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.core.util.StringUtils;
import org.spin.enhance.gson.MatchableTypeAdapter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalDateTypeAdapter extends MatchableTypeAdapter<LocalDate> {
    private DateTimeFormatter formater;

    public LocalDateTypeAdapter(String datePattern) {
        this.formater = DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(formater.format(value));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            return LocalDate.parse(tmp, formater);
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return LocalDate.class.isAssignableFrom(type.getRawType());
    }
}
