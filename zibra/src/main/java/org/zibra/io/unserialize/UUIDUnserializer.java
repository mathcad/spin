package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagBytes;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagGuid;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

public final class UUIDUnserializer extends BaseUnserializer<UUID> {

    public final static UUIDUnserializer instance = new UUIDUnserializer();

    @Override
    public UUID unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagEmpty: return null;
            case TagString: return UUID.fromString(ReferenceReader.readString(reader));
            case TagBytes: return UUID.nameUUIDFromBytes(ReferenceReader.readBytes(reader));
            case TagGuid: return ReferenceReader.readUUID(reader);
        }
        return super.unserialize(reader, tag, type);
    }

    public UUID read(Reader reader) throws IOException {
        return read(reader, UUID.class);
    }
}
