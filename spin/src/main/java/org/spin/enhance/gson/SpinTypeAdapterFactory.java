package org.spin.enhance.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.proxy.HibernateProxy;
import org.spin.core.annotation.UserEnum;
import org.spin.enhance.gson.adapter.HibernatePersistentBagTypeAdapter;
import org.spin.enhance.gson.adapter.HibernateProxyTypeAdapter;
import org.spin.enhance.gson.adapter.LocalDateTimeTypeAdapter;
import org.spin.enhance.gson.adapter.LocalDateTypeAdapter;
import org.spin.enhance.gson.adapter.LocalTimeTypeAdapter;
import org.spin.enhance.gson.adapter.LongTypeAdapter;
import org.spin.enhance.gson.adapter.UserEnumTypeAdapter;
import org.spin.enhance.gson.annotation.PreventOverflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Gson TypeAdapter
 * <p>Created by xuweinan on 2017/1/25.</p>
 *
 * @author xuweinan
 */
public class SpinTypeAdapterFactory implements TypeAdapterFactory {
    private String datePattern;

    public SpinTypeAdapterFactory() {
    }

    public SpinTypeAdapterFactory(String datePattern) {
        this.datePattern = datePattern;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (HibernateProxy.class.isAssignableFrom(type.getRawType()))
            return (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson);
        else if (PersistentBag.class.isAssignableFrom(type.getRawType()))
            return (TypeAdapter<T>) new HibernatePersistentBagTypeAdapter();
        else if (LocalDateTime.class.isAssignableFrom(type.getRawType()))
            return (TypeAdapter<T>) new LocalDateTimeTypeAdapter(datePattern);
        else if (LocalDate.class.isAssignableFrom(type.getRawType()))
            return (TypeAdapter<T>) new LocalDateTypeAdapter(datePattern);
        else if (LocalTime.class.isAssignableFrom(type.getRawType()))
            return (TypeAdapter<T>) new LocalTimeTypeAdapter(datePattern);
        else if (Enum.class.isAssignableFrom(type.getRawType()) && type.getRawType().getAnnotation(UserEnum.class) != null)
            return new UserEnumTypeAdapter(type.getRawType());
        else if (Long.class.isAssignableFrom(type.getRawType()) && type.getRawType().getAnnotation(PreventOverflow.class) != null) {
            return (TypeAdapter<T>) new LongTypeAdapter();
        }
        else
            return null;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
}
