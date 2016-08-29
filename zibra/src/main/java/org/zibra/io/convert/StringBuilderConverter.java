package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;

public class StringBuilderConverter implements Converter<StringBuilder> {

    public final static StringBuilderConverter instance = new StringBuilderConverter();

    public StringBuilder convertTo(char[] chars) {
        return new StringBuilder(chars.length + 16).append(chars);
    }
    public StringBuilder convertTo(Object obj, Type type) {
        if (obj instanceof char[]) {
            return convertTo((char[]) obj);
        }
        else if (obj instanceof String) {
            return new StringBuilder((String) obj);
        }
        else if (obj instanceof DateTime) {
            return ((DateTime) obj).toStringBuilder();
        }
        return new StringBuilder(obj.toString());
    }
}
