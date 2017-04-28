package org.spin.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spin.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
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
}
