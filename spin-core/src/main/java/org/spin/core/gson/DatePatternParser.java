package org.spin.core.gson;

import org.spin.core.collection.Pair;
import org.spin.core.collection.Tuple;
import org.spin.core.gson.annotation.DatePattern;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;

import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

    private static final Map<String, Pair<List<DateTimeFormatter>, DateTimeFormatter>> FORMATS = new ConcurrentHashMap<>(64);

    public static List<DateTimeFormatter> getReadPattern(DateTimeFormatter defaultPattern, Field field) {
        if (null == field) {
            return Collections.singletonList(defaultPattern);
        }
        String name = field.getDeclaringClass().getName() + "." + field.getName();
        Pair<List<DateTimeFormatter>, DateTimeFormatter> dateTimeFormatter = getFormatter(field, name);
        return CollectionUtils.isEmpty(dateTimeFormatter.c1) ? Collections.singletonList(defaultPattern) : dateTimeFormatter.c1;
    }

    public static DateTimeFormatter getWritePattern(DateTimeFormatter defaultPattern, Field field) {
        if (null == field) {
            return defaultPattern;
        }
        String name = field.getDeclaringClass().getName() + "." + field.getName();
        Pair<List<DateTimeFormatter>, DateTimeFormatter> dateTimeFormatter = getFormatter(field, name);
        return null == dateTimeFormatter.c2 ? defaultPattern : dateTimeFormatter.c2;
    }

    private static Pair<List<DateTimeFormatter>, DateTimeFormatter> getFormatter(Field field, String name) {
        Pair<List<DateTimeFormatter>, DateTimeFormatter> dateTimeFormatter;

        List<DateTimeFormatter> read = new LinkedList<>();
        DateTimeFormatter write = null;
        if (FORMATS.containsKey(name)) {
            dateTimeFormatter = FORMATS.get(name);
        } else {
            DatePattern dp = field.getAnnotation(DatePattern.class);
            if (dp != null) {
                if (StringUtils.isNotEmpty(dp.write())) {
                    write = DateTimeFormatter.ofPattern(dp.write());
                }
                String[] rp = dp.read();
                if (rp != null && rp.length > 0) {
                    for (String s : rp) {
                        if (StringUtils.isNotEmpty(s)) {
                            read.add(DateTimeFormatter.ofPattern(s));
                        }
                    }
                }
            }
            dateTimeFormatter = Tuple.of(read, write);
            FORMATS.put(name, dateTimeFormatter);
        }

        return dateTimeFormatter;
    }
}
