package org.zibra.io.convert;

import java.lang.reflect.Type;
import java.util.TimeZone;

public class TimeZoneConverter implements Converter<TimeZone> {

    public final static TimeZoneConverter instance = new TimeZoneConverter();

    public TimeZone convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return TimeZone.getTimeZone((String) obj);
        }
        else if (obj instanceof char[]) {
            return TimeZone.getTimeZone(new String((char[]) obj));
        }
        return (TimeZone) obj;
    }
}
