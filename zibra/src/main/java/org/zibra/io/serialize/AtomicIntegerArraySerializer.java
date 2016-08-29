package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class AtomicIntegerArraySerializer extends ReferenceSerializer<AtomicIntegerArray> {

    public final static AtomicIntegerArraySerializer instance = new AtomicIntegerArraySerializer();

    @Override
    public final void serialize(Writer writer, AtomicIntegerArray array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length();
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            ValueWriter.write(stream, array.get(i));
        }
        stream.write(TagClosebrace);
    }
}
