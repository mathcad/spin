package org.spin.enhance.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * lazy-load gson序列化 解决方案
 * 序列化hibernate代理实体对象的id字段
 *
 * @author xuewinan
 */
public class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {
    private static final Logger logger = LoggerFactory.getLogger(HibernateProxyTypeAdapter.class);
    private final Gson context;

    public HibernateProxyTypeAdapter(Gson context) {
        this.context = context;
    }

    @Override
    public HibernateProxy read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(JsonWriter out, HibernateProxy value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        Class<?> baseType = javassist.util.proxy.ProxyFactory.isProxyClass(value.getClass()) ? value.getClass().getSuperclass() : value.getClass();
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
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(e.getMessage());
            }
            delegate.write(out, o);
        }
    }
}
