package org.zibra.io.convert;

import org.zibra.io.unserialize.ValueReader;
import java.lang.reflect.Type;

public class DoubleConverter implements Converter<Double> {

    public final static DoubleConverter instance = new DoubleConverter();

    public Double convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return ValueReader.parseDouble((String) obj);
        }
        else if (obj instanceof char[]) {
            return ValueReader.parseDouble(new String((char[]) obj));
        }
        return (Double) obj;
    }
}
