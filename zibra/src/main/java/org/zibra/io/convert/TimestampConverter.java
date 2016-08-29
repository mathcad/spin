package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.sql.Timestamp;

public class TimestampConverter implements Converter<Timestamp> {

    public final static TimestampConverter instance = new TimestampConverter();

    public Timestamp convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return ((DateTime) obj).toTimestamp();
        }
        else if (obj instanceof String) {
            return Timestamp.valueOf((String) obj);
        }
        else if (obj instanceof char[]) {
            return Timestamp.valueOf(new String((char[]) obj));
        }
        else if (obj instanceof Long) {
            return new Timestamp((Long) obj);
        }
        else if (obj instanceof Double) {
            return new Timestamp(((Double) obj).longValue());
        }
        return (Timestamp) obj;
    }
}
