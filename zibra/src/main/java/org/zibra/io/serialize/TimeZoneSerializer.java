package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TimeZone;

public final class TimeZoneSerializer extends ReferenceSerializer<TimeZone> {

    public final static TimeZoneSerializer instance = new TimeZoneSerializer();

    @Override
    public final void serialize(Writer writer, TimeZone obj) throws IOException {
        super.serialize(writer, obj);
        OutputStream stream = writer.stream;
        stream.write(TagString);
        ValueWriter.write(stream, obj.getID());
    }
}
