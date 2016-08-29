package org.zibra.io.serialize.java8;

import org.zibra.io.serialize.ReferenceSerializer;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagUTC;
import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetTime;
import java.time.ZoneOffset;

public final class OffsetTimeSerializer extends ReferenceSerializer<OffsetTime> {

    public final static OffsetTimeSerializer instance = new OffsetTimeSerializer();

    @Override
    public final void serialize(Writer writer, OffsetTime time) throws IOException {
        super.serialize(writer, time);
        OutputStream stream = writer.stream;
        if (!(time.getOffset().equals(ZoneOffset.UTC))) {
            stream.write(TagString);
            ValueWriter.write(stream, time.toString());
        }
        else {
            ValueWriter.writeTime(stream, time.getHour(), time.getMinute(), time.getSecond(), 0, false, true);
            ValueWriter.writeNano(stream, time.getNano());
            stream.write(TagUTC);
        }
    }
}
