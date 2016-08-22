package org.infrastructure.jpa.api;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.collection.internal.PersistentBag;

import java.io.IOException;

/**
 * lazy-load 解决方案
 */
public class HibernatePersistentBagTypeAdapter extends TypeAdapter<PersistentBag> {
    private Gson context;
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (PersistentBag.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernatePersistentBagTypeAdapter(gson) : null);
        }
    };

    private HibernatePersistentBagTypeAdapter(Gson context) {
        this.context = context;
    }

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
