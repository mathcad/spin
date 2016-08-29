package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;

public class StringBufferConverter implements Converter<StringBuffer> {

    public final static StringBufferConverter instance = new StringBufferConverter();

    public StringBuffer convertTo(char[] chars) {
        return new StringBuffer(chars.length + 16).append(chars);
    }
    public StringBuffer convertTo(Object obj, Type type) {
        if (obj instanceof char[]) {
            return convertTo((char[]) obj);
        }
        else if (obj instanceof String) {
            return new StringBuffer((String) obj);
        }
        else if (obj instanceof DateTime) {
            return ((DateTime) obj).toStringBuffer();
        }
        return new StringBuffer(obj.toString());
    }
}
