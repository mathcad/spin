package org.zibra.io.convert;

import java.lang.reflect.Type;

public class CharConverter implements Converter<Character> {

    public final static CharConverter instance = new CharConverter();

    public Character convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return ((String) obj).charAt(0);
        }
        else if (obj instanceof char[]) {
            return ((char[]) obj)[0];
        }
        return (Character) obj;
    }
}
