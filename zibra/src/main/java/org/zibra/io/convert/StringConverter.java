package org.zibra.io.convert;

import java.lang.reflect.Type;

public class StringConverter implements Converter<String> {

    public final static StringConverter instance = new StringConverter();

    public String convertTo(Object obj, Type type) {
        if (obj instanceof char[]) {
            return new String((char[]) obj);
        }
        else if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }
}
