package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

public final class OtherTypeArraySerializer extends ReferenceSerializer {

    public final static OtherTypeArraySerializer instance = new OtherTypeArraySerializer();

    @Override
    @SuppressWarnings({"unchecked"})
    public final void serialize(Writer writer, Object array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        int length = Array.getLength(array);
        stream.write(TagList);
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            writer.serialize(Array.get(array, i));
        }
        stream.write(TagClosebrace);
    }
}
