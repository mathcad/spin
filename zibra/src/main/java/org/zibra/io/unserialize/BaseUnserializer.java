package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagClass;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagRef;
import org.zibra.util.ClassUtil;
import java.io.IOException;
import java.lang.reflect.Type;

public abstract class BaseUnserializer<T> implements Unserializer<T> {

    public T unserialize(Reader reader, int tag, Type type) throws IOException {
        throw ValueReader.castError(reader.tagToString(tag), ClassUtil.toClass(type));
    }

    @SuppressWarnings({"unchecked"})
    public T read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return null;
        if (tag == TagRef) return (T) reader.readRef(type);
        if (tag == TagClass) {
            reader.readClass();
            return read(reader, type);
        }
        return unserialize(reader, tag, type);
    }

    public T read(Reader reader, Type type) throws IOException {
        return read(reader, reader.stream.read(), type);
    }
}
