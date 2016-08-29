package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import java.lang.reflect.Type;
import java.time.ZoneOffset;

public class ZoneOffsetConverter implements Converter<ZoneOffset> {

    public final static ZoneOffsetConverter instance = new ZoneOffsetConverter();

    public ZoneOffset convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return ZoneOffset.of((String) obj);
        }
        else if (obj instanceof char[]) {
            return ZoneOffset.of(new String((char[]) obj));
        }
        return (ZoneOffset) obj;
    }
}
