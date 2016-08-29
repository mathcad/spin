package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;

public final class ShortArraySerializer extends ReferenceSerializer<short[]> {

    public final static ShortArraySerializer instance = new ShortArraySerializer();

    @Override
    public final void serialize(Writer writer, short[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            ValueWriter.write(stream, array[i]);
        }
        stream.write(TagClosebrace);
    }
}
