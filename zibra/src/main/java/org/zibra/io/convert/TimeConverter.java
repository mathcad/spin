package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.sql.Time;

public class TimeConverter implements Converter<Time> {

    public final static TimeConverter instance = new TimeConverter();

    public Time convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return ((DateTime) obj).toTime();
        }
        else if (obj instanceof String) {
            return Time.valueOf((String) obj);
        }
        else if (obj instanceof char[]) {
            return Time.valueOf(new String((char[]) obj));
        }
        else if (obj instanceof Long) {
            return new Time((Long) obj);
        }
        else if (obj instanceof Double) {
            return new Time(((Double) obj).longValue());
        }
        return (Time) obj;
    }
}
