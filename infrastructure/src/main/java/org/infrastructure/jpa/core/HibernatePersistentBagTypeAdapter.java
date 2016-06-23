package org.infrastructure.jpa.core;

import java.io.IOException;

import org.hibernate.collection.internal.PersistentBag;
import org.infrastructure.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * lazy-load 解决方案
 * 
 * @author zhou
 *
 */
public class HibernatePersistentBagTypeAdapter extends TypeAdapter<PersistentBag> {
	static final Logger logger = LoggerFactory.getLogger(HibernatePersistentBagTypeAdapter.class);

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			logger.info(ObjectUtils.toString(type.getType(), "null"));
			logger.info(ObjectUtils.toString(type.getRawType(), "null"));

			return (PersistentBag.class.isAssignableFrom(type.getRawType())
					? (TypeAdapter<T>) new HibernatePersistentBagTypeAdapter(gson) : null);
		}
	};

	private HibernatePersistentBagTypeAdapter(Gson context) {
	}

	@Override
	public PersistentBag read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void write(JsonWriter out, PersistentBag value) throws IOException {
		logger.info("gson PersistentBag lazyload");
		logger.info(ObjectUtils.toString(value, "null"));

		if (value == null) {
			out.nullValue();
			return;
		} else {
			// 写入空数组
			out.beginArray().endArray();
		}
	}
}
