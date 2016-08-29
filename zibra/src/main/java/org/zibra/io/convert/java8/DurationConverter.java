package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import java.lang.reflect.Type;
import java.time.Duration;

public class DurationConverter implements Converter<Duration> {

    public final static DurationConverter instance = new DurationConverter();

    public Duration convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return Duration.parse((String) obj);
        }
        else if (obj instanceof char[]) {
            return Duration.parse(new String((char[]) obj));
        }
        else if (obj instanceof Long) {
            return Duration.ofNanos((Long) obj);
        }
        else if (obj instanceof Double) {
            return Duration.ofNanos(((Double) obj).longValue());
        }
        return (Duration) obj;
    }
}
