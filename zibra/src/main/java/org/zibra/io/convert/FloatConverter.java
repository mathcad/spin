package org.zibra.io.convert;

import org.zibra.io.unserialize.ValueReader;
import java.lang.reflect.Type;

public class FloatConverter implements Converter<Float> {

    public final static FloatConverter instance = new FloatConverter();

    public Float convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return ValueReader.parseFloat((String) obj);
        }
        else if (obj instanceof char[]) {
            return ValueReader.parseFloat(new String((char[]) obj));
        }
        return (Float) obj;
    }
}
