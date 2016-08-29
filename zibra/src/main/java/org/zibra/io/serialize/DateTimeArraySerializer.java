package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public final class DateTimeArraySerializer extends ReferenceSerializer<Date[]> {

    public final static DateTimeArraySerializer instance = new DateTimeArraySerializer();

    @Override
    public final void serialize(Writer writer, Date[] array) throws IOException {
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
            Date e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                DateTimeSerializer.instance.write(writer, e);
            }
            ++i;
        }
        stream.write(TagClosebrace);
    }
}