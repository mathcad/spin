package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    public final static LocalDateTimeConverter instance = new LocalDateTimeConverter();

    public LocalDateTime convertTo(DateTime dt) {
        return LocalDateTime.of(dt.year, dt.month, dt.day, dt.hour, dt.minute, dt.second, dt.nanosecond);
    }

    public LocalDateTime convertTo(String str) {
        return LocalDateTime.parse(str);
    }

    public LocalDateTime convertTo(char[] chars) {
        return LocalDateTime.parse(new String(chars));
    }

    public LocalDateTime convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return convertTo((DateTime) obj);
        }
        else if (obj instanceof String) {
            return LocalDateTime.parse((String) obj);
        }
        else if (obj instanceof char[]) {
            return LocalDateTime.parse(new String((char[])obj));
        }
        return (LocalDateTime) obj;
    }
}
