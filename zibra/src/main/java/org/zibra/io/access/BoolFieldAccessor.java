package org.zibra.io.access;

import org.zibra.common.ZibraException;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.BooleanUnserializer;
import org.zibra.io.unserialize.Reader;
import java.io.IOException;
import java.lang.reflect.Field;

public final class BoolFieldAccessor implements MemberAccessor {
    private final long offset;

    public BoolFieldAccessor(Field accessor) {
        accessor.setAccessible(true);
        offset = Accessors.unsafe.objectFieldOffset(accessor);
    }

    @Override
    public final void serialize(Writer writer, Object obj) throws IOException {
        boolean value;
        try {
            value = Accessors.unsafe.getBoolean(obj, offset);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
        ValueWriter.write(writer.stream, value);
    }

    @Override
    public final void unserialize(Reader reader, Object obj) throws IOException {
        boolean value = BooleanUnserializer.instance.read(reader);
        try {
            Accessors.unsafe.putBoolean(obj, offset, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }
}