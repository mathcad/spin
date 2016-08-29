package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.time.Year;

public class YearConverter implements Converter<Year> {

    public final static YearConverter instance = new YearConverter();

    public Year convertTo(DateTime dt) {
        return Year.of(dt.year);
    }

    public Year convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return convertTo((DateTime) obj);
        }
        else if (obj instanceof String) {
            return Year.parse((String) obj);
        }
        else if (obj instanceof char[]) {
            return Year.parse(new String((char[]) obj));
        }
        return (Year) obj;
    }
}
