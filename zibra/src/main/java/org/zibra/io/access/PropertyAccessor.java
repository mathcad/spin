package org.zibra.io.access;

import org.zibra.common.ZibraException;
import static org.zibra.io.Tags.TagNull;

import org.zibra.io.serialize.Serializer;
import org.zibra.io.serialize.SerializerFactory;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.Unserializer;
import org.zibra.io.unserialize.UnserializerFactory;
import org.zibra.util.ClassUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class PropertyAccessor implements MemberAccessor {
    private final static Object[] nullArgs = new Object[0];
    private final Method getter;
    private final Method setter;
    private final Type propType;
    private final Serializer serializer;
    private final Unserializer unserializer;

    public PropertyAccessor(Type type, Method getter, Method setter) {
        getter.setAccessible(true);
        setter.setAccessible(true);
        this.getter = getter;
        this.setter = setter;
        propType = ClassUtil.getActualType(type, getter.getGenericReturnType());
        Class<?> cls = ClassUtil.toClass(propType);
        serializer = SerializerFactory.get(cls);
        unserializer = UnserializerFactory.get(cls);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void serialize(Writer writer, Object obj) throws IOException {
        Object value;
        try {
            value = getter.invoke(obj, nullArgs);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
        if (value == null) {
            writer.stream.write(TagNull);
        }
        else {
            serializer.write(writer, value);
        }
    }

    @Override
    public void unserialize(Reader reader, Object obj) throws IOException {
        Object value = unserializer.read(reader, reader.stream.read(), propType);
        try {
            setter.invoke(obj, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }

}