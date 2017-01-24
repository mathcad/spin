package org.spin.gson;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.spin.throwable.SimplifiedException;
import org.spin.util.DateUtils;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Timestamp的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class TimestampTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
    private DateFormat format;
    private String datePattern;

    public TimestampTypeAdapter(String datePattern) {
        this.datePattern = datePattern;
        this.format = new SimpleDateFormat(datePattern);
    }

    @Override
    public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
        String dfString = format.format(new Date(src.getTime()));
        return new JsonPrimitive(dfString);
    }

    @Override
    public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }

        try {
            Date date = DateUtils.parseDate(json.getAsString());
            return new Timestamp(date.getTime());
        } catch (SimplifiedException e) {
            throw new JsonParseException(e);
        }
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
        this.format = new SimpleDateFormat(datePattern);
    }
}