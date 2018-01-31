package org.spin.enhance.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.ConstructorUtils;
import org.spin.core.util.StringUtils;
import org.spin.enhance.gson.adapter.LocalDateTimeTypeAdapter;
import org.spin.enhance.gson.adapter.LocalDateTypeAdapter;
import org.spin.enhance.gson.adapter.LocalTimeTypeAdapter;
import org.spin.enhance.gson.adapter.LongTypeAdapter;
import org.spin.enhance.gson.adapter.UserEnumTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gson TypeAdapterFactory
 * <p>Created by xuweinan on 2017/1/25.</p>
 *
 * @author xuweinan
 */
public class SpinTypeAdapterFactory implements TypeAdapterFactory {
    private String datePattern;
    private String localDatePatern;
    private String localTimePatern;
    private static List<MatchableTypeAdapter<?>> typeAdapters = new ArrayList<>();

    public SpinTypeAdapterFactory(String datePattern, String localDatePatern, String localTimePatern) {
        this.datePattern = datePattern;
        this.localDatePatern = localDatePatern;
        this.localTimePatern = localTimePatern;
    }

    @SuppressWarnings("unchecked")
    private <T> void init(Gson gson, TypeToken<T> type) {
        try {
            Class<MatchableTypeAdapter> cls = (Class<MatchableTypeAdapter>) ClassUtils.getClass("org.spin.enhance.gson.adapter.HibernateProxyTypeAdapter");
            typeAdapters.add(ConstructorUtils.invokeConstructor(cls, gson));
            cls = (Class<MatchableTypeAdapter>) ClassUtils.getClass("org.spin.enhance.gson.adapter.HibernatePersistentBagTypeAdapter");
            typeAdapters.add(cls.getConstructor().newInstance());
        } catch (Exception ignore) {
        }
        typeAdapters.add(new LocalDateTimeTypeAdapter(datePattern));
        typeAdapters.add(new LocalDateTypeAdapter(localDatePatern));
        typeAdapters.add(new LocalTimeTypeAdapter(localTimePatern));
        typeAdapters.add(new LongTypeAdapter());
        typeAdapters.add(new UserEnumTypeAdapter());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (typeAdapters.isEmpty()) {
            init(gson, type);
        }
        return (TypeAdapter<T>) typeAdapters.stream().filter(t -> t.isMatch(type)).findFirst().orElse(null);
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
}
