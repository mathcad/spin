package org.spin.data.gson.adapter;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.gson.Gson;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.TypeAdapter;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * lazy-load gson序列化 解决方案
 * 序列化hibernate代理实体对象的id字段
 *
 * @author xuewinan
 */
public class HibernateProxyTypeAdapter extends MatchableTypeAdapter<HibernateProxy> {
    private static final Logger logger = LoggerFactory.getLogger(HibernateProxyTypeAdapter.class);
    private final Gson context;

    public HibernateProxyTypeAdapter(Gson context) {
        this.context = context;
    }

    @Override
    public HibernateProxy read(JsonReader in, TypeToken<?> type, Field field) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(JsonWriter out, HibernateProxy value, Field field) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        Class<?> baseType =HibernateProxyHelper.getClassWithoutInitializingProxy(value);
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

    @Override
    public boolean isMatch(TypeToken<?> type) {
        return HibernateProxy.class.isAssignableFrom(type.getRawType());
    }
}
