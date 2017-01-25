package org.spin.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class LocalTimeTypeAdapter extends TypeAdapter<LocalTime> {
    private DateTimeFormatter formater;

    public LocalTimeTypeAdapter(String datePattern) {
        this.formater = DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
        out.value(formater.format(value));
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
        return LocalTime.parse(in.nextString(), formater);
    }
}