package org.spin.data.gson.adapter;

import org.hibernate.collection.internal.PersistentBag;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * lazy-load 解决方案
 */
public class HibernatePersistentBagTypeAdapter extends MatchableTypeAdapter<PersistentBag> {

    @Override
    public PersistentBag read(JsonReader in, TypeToken<?> type, Field field) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void write(JsonWriter out, PersistentBag value, Field field) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // 写入空数组
            out.beginArray().endArray();
        }
    }

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return PersistentBag.class.isAssignableFrom(type.getRawType());
    }
}
