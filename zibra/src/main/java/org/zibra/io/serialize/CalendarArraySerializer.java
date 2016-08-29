package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public final class CalendarArraySerializer extends ReferenceSerializer<Calendar[]> {

    public final static CalendarArraySerializer instance = new CalendarArraySerializer();

    @Override
    public final void serialize(Writer writer, Calendar[] array) throws IOException {
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
            Calendar e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                CalendarSerializer.instance.write(writer, e);
            }
            ++i;
        }
        stream.write(TagClosebrace);
    }
}
