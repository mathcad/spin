package org.zibra.io.access;

import org.zibra.common.ZibraException;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ShortUnserializer;
import java.io.IOException;
import java.lang.reflect.Field;

public final class ShortFieldAccessor implements MemberAccessor {
    private final long offset;

    public ShortFieldAccessor(Field accessor) {
        accessor.setAccessible(true);
        offset = Accessors.unsafe.objectFieldOffset(accessor);
    }

    @Override
    public void serialize(Writer writer, Object obj) throws IOException {
        int value;
        try {
            value = Accessors.unsafe.getShort(obj, offset);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
        ValueWriter.write(writer.stream, value);
    }

    @Override
    public void unserialize(Reader reader, Object obj) throws IOException {
        short value = ShortUnserializer.instance.read(reader);
        try {
            Accessors.unsafe.putShort(obj, offset, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }
}