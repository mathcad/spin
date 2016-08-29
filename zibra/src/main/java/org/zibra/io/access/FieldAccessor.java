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
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public final class FieldAccessor implements MemberAccessor {
    private final long offset;
    private final Type fieldType;
    private final Serializer serializer;
    private final Unserializer unserializer;

    public FieldAccessor(Type type, Field field) {
        field.setAccessible(true);
        offset = Accessors.unsafe.objectFieldOffset(field);
        fieldType = ClassUtil.getActualType(type, field.getGenericType());
        Class<?> cls = ClassUtil.toClass(fieldType);
        serializer = SerializerFactory.get(cls);
        unserializer = UnserializerFactory.get(cls);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void serialize(Writer writer, Object obj) throws IOException {
        Object value;
        try {
            value = Accessors.unsafe.getObject(obj, offset);
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
        Object value = unserializer.read(reader, reader.stream.read(), fieldType);
        try {
            Accessors.unsafe.putObject(obj, offset, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }
}