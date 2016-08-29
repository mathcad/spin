package org.zibra.io.access;

import org.zibra.common.ZibraException;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.ByteUnserializer;
import org.zibra.io.unserialize.Reader;
import java.io.IOException;
import java.lang.reflect.Field;

public final class ByteFieldAccessor implements MemberAccessor {
    private final long offset;

    public ByteFieldAccessor(Field accessor) {
        accessor.setAccessible(true);
        offset = Accessors.unsafe.objectFieldOffset(accessor);
    }

    @Override
    public void serialize(Writer writer, Object obj) throws IOException {
        int value;
        try {
            value = Accessors.unsafe.getByte(obj, offset);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
        ValueWriter.write(writer.stream, value);
    }

    @Override
    public void unserialize(Reader reader, Object obj) throws IOException {
        byte value = ByteUnserializer.instance.read(reader);
        try {
            Accessors.unsafe.putInt(obj, offset, value);
        }
        catch (Exception e) {
            throw new ZibraException(e.getMessage());
        }
    }
}