package org.spin.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.collection.internal.PersistentBag;

import java.io.IOException;

/**
 * lazy-load 解决方案
 */
public class HibernatePersistentBagTypeAdapter extends TypeAdapter<PersistentBag> {

    @Override
    public PersistentBag read(JsonReader in) throws IOException {
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
}
