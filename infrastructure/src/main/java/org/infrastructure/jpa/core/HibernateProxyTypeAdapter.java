package org.infrastructure.jpa.core;

import java.io.IOException;
import java.lang.reflect.Field;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * lazy-load gson序列化 解决方案
 * 
 * @author zhou
 *
 */
public class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {
	static final Logger logger = LoggerFactory.getLogger(HibernateProxyTypeAdapter.class);

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (HibernateProxy.class.isAssignableFrom(type.getRawType())
					? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
		}
	};
	private final Gson context;

	private HibernateProxyTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public HibernateProxy read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void write(JsonWriter out, HibernateProxy value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		Class<?> baseType = (javassist.util.proxy.ProxyFactory.isProxyClass(value.getClass()))
				? value.getClass().getSuperclass() : value.getClass();
		TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));

		Object o = null;
		if (Hibernate.isInitialized(value)) {
			delegate.write(out, value);
		} else {
			try {
				o = baseType.newInstance();
				Field f = ReflectionUtils.findField(baseType, "id");
				ReflectionUtils.makeAccessible(f);
				f.set(o, value.getHibernateLazyInitializer().getIdentifier());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			delegate.write(out, o);
		}
	}
}
