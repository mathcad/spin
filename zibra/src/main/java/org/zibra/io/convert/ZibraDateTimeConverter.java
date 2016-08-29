package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.util.Date;

public class ZibraDateTimeConverter implements Converter<DateTime> {

    public final static ZibraDateTimeConverter instance = new ZibraDateTimeConverter();

    @SuppressWarnings({"deprecation"})
    public DateTime convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return (DateTime) obj;
        }
        else if (obj instanceof String) {
            return new DateTime(new Date((String) obj));
        }
        else if (obj instanceof char[]) {
            return new DateTime(new Date(new String((char[]) obj)));
        }
        else if (obj instanceof Long) {
            return new DateTime(new Date((Long) obj));
        }
        else if (obj instanceof Double) {
            return new DateTime(new Date(((Double) obj).longValue()));
        }
        // TODO: 2016/8/29 xuweinan 日期转换待完善
        return (DateTime) obj;
    }
}
