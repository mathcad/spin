package org.zibra.io.convert;

import java.lang.reflect.Type;

public class BooleanConverter implements Converter<Boolean> {

    public final static BooleanConverter instance = new BooleanConverter();

    public Boolean convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        else if (obj instanceof char[]) {
            return Boolean.parseBoolean(new String((char[]) obj));
        }
        return (Boolean) obj;
    }
}
