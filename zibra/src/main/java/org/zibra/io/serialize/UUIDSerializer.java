package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagGuid;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public final class UUIDSerializer extends ReferenceSerializer<UUID> {

    public final static UUIDSerializer instance = new UUIDSerializer();

    @Override
    public final void serialize(Writer writer, UUID uuid) throws IOException {
        super.serialize(writer, uuid);
        OutputStream stream = writer.stream;
        stream.write(TagGuid);
        stream.write(TagOpenbrace);
        stream.write(ValueWriter.getAscii(uuid.toString()));
        stream.write(TagClosebrace);
    }
}
