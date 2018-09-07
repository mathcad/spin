package org.spin.core.gson;

import org.spin.core.gson.annotation.DatePattern;
import org.spin.core.util.StringUtils;

import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created by xuweinan on 2018/3/10.</p>
 *
 * @author xuweinan
 */
public final class DatePatternParser {

    private DatePatternParser() {
    }

    private static final DateTimeFormatter EMPTY = DateTimeFormatter.ofPattern("");
    private static final Map<String, DateTimeFormatter> FORMATS = new ConcurrentHashMap<>(64);

    public static DateTimeFormatter getReadPattern(DateTimeFormatter defaultPattern, Field field) {
        if (null == field) {
            return defaultPattern;
        }
        String name = field.getDeclaringClass().getName() + "." + field.getName() + "-read";
        DateTimeFormatter dateTimeFormatter = getFormatter(field, name);
        return EMPTY == dateTimeFormatter || null == dateTimeFormatter ? defaultPattern : dateTimeFormatter;
    }

    public static DateTimeFormatter getWritePattern(DateTimeFormatter defaultPattern, Field field) {
        if (null == field) {
            return defaultPattern;
        }
        String name = field.getDeclaringClass().getName() + "." + field.getName() + "-write";
        DateTimeFormatter dateTimeFormatter = getFormatter(field, name);
        return EMPTY == dateTimeFormatter || null == dateTimeFormatter ? defaultPattern : dateTimeFormatter;
    }

    private static DateTimeFormatter getFormatter(Field field, String name) {
        DateTimeFormatter dateTimeFormatter = null;

        if (FORMATS.containsKey(name)) {
            dateTimeFormatter = FORMATS.get(name);
        } else {
            DatePattern dp = field.getAnnotation(DatePattern.class);
            if (dp != null) {
                if (StringUtils.isNotEmpty(dp.write())) {
                    dateTimeFormatter = DateTimeFormatter.ofPattern(dp.write());
                    FORMATS.put(name, dateTimeFormatter);
                } else if (StringUtils.isNotEmpty(dp.read())) {
                    dateTimeFormatter = DateTimeFormatter.ofPattern(dp.read());
                    FORMATS.put(name, dateTimeFormatter);
                } else {
                    FORMATS.put(name, EMPTY);
                }
            }
        }

        return dateTimeFormatter;
    }
}
