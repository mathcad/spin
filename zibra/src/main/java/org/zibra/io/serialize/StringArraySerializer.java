package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;

public final class StringArraySerializer extends ReferenceSerializer<String[]> {

    public final static StringArraySerializer instance = new StringArraySerializer();

    @Override
    public final void serialize(Writer writer, String[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            String e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                StringSerializer.instance.write(writer, e);
            }
        }
        stream.write(TagClosebrace);
    }
}
