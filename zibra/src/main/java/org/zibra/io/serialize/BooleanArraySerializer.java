package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;

public final class BooleanArraySerializer extends ReferenceSerializer<boolean[]> {

    public final static BooleanArraySerializer instance = new BooleanArraySerializer();

    @Override
    public final void serialize(Writer writer, boolean[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        int i = 0;
        while (i < length) {
            ValueWriter.write(stream, array[i]);
            ++i;
        }
        stream.write(TagClosebrace);
    }
}