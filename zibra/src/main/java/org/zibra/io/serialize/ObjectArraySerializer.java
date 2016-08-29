package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;

public final class ObjectArraySerializer extends ReferenceSerializer<Object[]> {

    public final static ObjectArraySerializer instance = new ObjectArraySerializer();

    @Override
    public final void serialize(Writer writer, Object[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        int length = array.length;
        stream.write(TagList);
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            writer.serialize(array[i]);
        }
        stream.write(TagClosebrace);
    }
}
