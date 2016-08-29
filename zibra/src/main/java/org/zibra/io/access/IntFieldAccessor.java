package org.zibra.io.access;

import org.zibra.common.ZibraException;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.IntUnserializer;
import org.zibra.io.unserialize.Reader;
import java.io.IOException;
import java.lang.reflect.Field;

public final class IntFieldAccessor implements MemberAccessor {
    private final long offset;

    public IntFieldAccessor(Field accessor) {
        accessor.setAccessible(true);
        offset = Accessors.unsafe.objectFieldOffset(accessor);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void serialize(Writer writer, Object obj) throws IOException {
        int value;
        try {
            value = Accessors.unsafe.getInt(obj, offset);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
        ValueWriter.write(writer.stream, value);
    }

    @Override
    public void unserialize(Reader reader, Object obj) throws IOException {
        int value = IntUnserializer.instance.read(reader);
        try {
            Accessors.unsafe.putInt(obj, offset, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }
}