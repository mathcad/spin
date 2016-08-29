package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

public final class UUIDArrayUnserializer extends BaseUnserializer<UUID[]> {

    public final static UUIDArrayUnserializer instance = new UUIDArrayUnserializer();

    @Override
    public UUID[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readUUIDArray(reader);
        if (tag == TagEmpty) return new UUID[0];
        return super.unserialize(reader, tag, type);
    }

    public UUID[] read(Reader reader) throws IOException {
        return read(reader, UUID[].class);
    }
}
