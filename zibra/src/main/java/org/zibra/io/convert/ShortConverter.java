package org.zibra.io.convert;

import java.lang.reflect.Type;

public class ShortConverter implements Converter<Short> {

    public final static ShortConverter instance = new ShortConverter();

    public Short convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return Short.parseShort((String) obj);
        }
        else if (obj instanceof char[]) {
            return Short.parseShort(new String((char[]) obj));
        }
        return (Short) obj;
    }
}
