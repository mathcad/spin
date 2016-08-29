package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import java.lang.reflect.Type;
import java.time.ZoneId;

public class ZoneIdConverter implements Converter<ZoneId> {

    public final static ZoneIdConverter instance = new ZoneIdConverter();

    public ZoneId convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return ZoneId.of((String) obj);
        }
        else if (obj instanceof char[]) {
            return ZoneId.of(new String((char[]) obj));
        }
        return (ZoneId) obj;
    }
}
