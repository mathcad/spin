package org.zibra.io.serialize.java8;

import org.zibra.io.serialize.ReferenceSerializer;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import static org.zibra.io.Tags.TagSemicolon;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalTime;

public final class LocalTimeSerializer extends ReferenceSerializer<LocalTime> {

    public final static LocalTimeSerializer instance = new LocalTimeSerializer();

    @Override
    public final void serialize(Writer writer, LocalTime time) throws IOException {
        super.serialize(writer, time);
        OutputStream stream = writer.stream;
        ValueWriter.writeTime(stream, time.getHour(), time.getMinute(), time.getSecond(), 0, false, true);
        ValueWriter.writeNano(stream, time.getNano());
        stream.write(TagSemicolon);
    }
}
