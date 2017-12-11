package org.spin.enhance.gson.adapter;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.collection.internal.PersistentBag;
import org.spin.enhance.gson.MatchableTypeAdapter;

import java.io.IOException;

/**
 * lazy-load 解决方案
 */
public class HibernatePersistentBagTypeAdapter extends MatchableTypeAdapter<PersistentBag> {

    @Override
    public PersistentBag read(JsonReader in) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void write(JsonWriter out, PersistentBag value) throws IOException {
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
