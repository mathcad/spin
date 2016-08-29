package org.zibra.io.convert;

import java.lang.reflect.Type;

public class ByteConverter implements Converter<Byte> {

    public final static ByteConverter instance = new ByteConverter();

    public Byte convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return Byte.parseByte((String) obj);
        }
        else if (obj instanceof char[]) {
            return Byte.parseByte(new String((char[]) obj));
        }
        return (Byte) obj;
    }
}
