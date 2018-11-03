package org.spin.core.gson.adapter;

import org.spin.core.gson.internal.JavaVersion;
import org.spin.core.gson.internal.PreJava9DateFormatProvider;
import org.spin.core.gson.internal.bind.util.ISO8601Utils;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.ErrorCode;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Date的GSON适配器
 * Created by xuweinan on 2017/1/17.
 *
 * @author xuweinan
 */
public class DateTypeAdapter extends MatchableTypeAdapter<Date> {
    private ThreadLocal<SimpleDateFormat> formater;

    /**
     * List of 1 or more different date formats used for de-serialization attempts.
     * The first of them (default US format) is used for serialization as well.
     */
    private final List<DateFormat> dateFormats = new ArrayList<>(3);

    public DateTypeAdapter(String datePattern) {
        this.formater = ThreadLocal.withInitial(() -> new SimpleDateFormat(datePattern));
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US));
        if (!Locale.getDefault().equals(Locale.US)) {
            dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT));
        }
        if (JavaVersion.isJava9OrLater()) {
            dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT));
        }
    }

    @Override
    public Date read(JsonReader in, TypeToken<?> type, Field field) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmp = in.nextString();
        if (StringUtils.isEmpty(tmp)) {
            return null;
        } else {
            try {
                return formater.get().parse(tmp);
            } catch (ParseException e) {
                return deserializeToDate(tmp);
            }
        }
    }

    private synchronized Date deserializeToDate(String json) {
        for (DateFormat dateFormat : dateFormats) {
            try {
                return dateFormat.parse(json);
            } catch (ParseException ignored) {
            }
        }
        try {
            return ISO8601Utils.parse(json, new ParsePosition(0));
        } catch (ParseException e) {
            throw new SimplifiedException(ErrorCode.DATEFORMAT_UNSUPPORT, e);
        }
    }

    @Override
    public void write(JsonWriter out, Date value, Field field) throws IOException {
        if (null == value) {
            out.nullValue();
        } else {
            out.value(formater.get().format(value));
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return Date.class == type.getRawType();
    }
}
