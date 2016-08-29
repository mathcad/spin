package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;

public final class StringBuilderArraySerializer extends ReferenceSerializer<StringBuilder[]> {

    public final static StringBuilderArraySerializer instance = new StringBuilderArraySerializer();

    @Override
    public final void serialize(Writer writer, StringBuilder[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        int length = array.length;
        stream.write(TagList);
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            StringBuilder e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                StringBuilderSerializer.instance.write(writer, e);
            }
        }
        stream.write(TagClosebrace);
    }
}
