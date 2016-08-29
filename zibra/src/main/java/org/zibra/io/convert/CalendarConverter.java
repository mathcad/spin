package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.util.Calendar;

public class CalendarConverter implements Converter<Calendar> {

    public final static CalendarConverter instance = new CalendarConverter();

    public Calendar convertTo(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    public Calendar convertTo(Double timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.longValue());
        return calendar;
    }

    public Calendar convertTo(Object obj, Type type) {
        if (obj instanceof DateTime) {
            return ((DateTime) obj).toCalendar();
        }
        else if (obj instanceof Long) {
            return convertTo((Long) obj);
        }
        else if (obj instanceof Double) {
            return convertTo((Double) obj);
        }
        return (Calendar) obj;
    }
}
