package org.zibra.io.convert.java8;

import org.zibra.io.convert.Converter;
import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.time.YearMonth;

public class YearMonthConverter implements Converter<YearMonth> {

    public final static YearMonthConverter instance = new YearMonthConverter();

    public YearMonth convertTo(DateTime dt) {
        return YearMonth.of(dt.year, dt.month);
    }

    public YearMonth convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return convertTo((DateTime) obj);
        }
        else if (obj instanceof String) {
            return YearMonth.parse((String) obj);
        }
        else if (obj instanceof char[]) {
            return YearMonth.parse(new String((char[]) obj));
        }
        return (YearMonth) obj;
    }
}
