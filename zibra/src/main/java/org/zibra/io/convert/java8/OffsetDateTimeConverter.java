package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import org.zibra.util.DateTime;
import org.zibra.util.TimeZoneUtil;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    public final static OffsetDateTimeConverter instance = new OffsetDateTimeConverter();

    public OffsetDateTime convertTo(DateTime dt) {
        return OffsetDateTime.of(dt.year, dt.month, dt.day,
                dt.hour, dt.minute, dt.second, dt.nanosecond,
                dt.utc ? ZoneOffset.UTC :
                         ZoneOffset.of(TimeZoneUtil.DefaultTZ.getID()));
    }

    public OffsetDateTime convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return convertTo((DateTime) obj);
        }
        else if (obj instanceof String) {
            return OffsetDateTime.parse((String) obj);
        }
        else if (obj instanceof char[]) {
            return OffsetDateTime.parse(new String((char[]) obj));
        }
        return (OffsetDateTime) obj;
    }
}
