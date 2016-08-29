package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;

public final class TimeArraySerializer extends ReferenceSerializer<Time[]> {

    public final static TimeArraySerializer instance = new TimeArraySerializer();

    @Override
    public final void serialize(Writer writer, Time[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            Time e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                TimeSerializer.instance.write(writer, e);
            }
        }
        stream.write(TagClosebrace);
    }
}
