package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagUTC;
import org.zibra.util.DateTime;
import java.io.IOException;
import java.io.OutputStream;

public final class ZibraDateTimeSerializer extends ReferenceSerializer<DateTime> {

    public final static ZibraDateTimeSerializer instance = new ZibraDateTimeSerializer();

    @Override
    public final void serialize(Writer writer, DateTime dt) throws IOException {
        super.serialize(writer, dt);
        OutputStream stream = writer.stream;
        if (dt.year == 1970 && dt.month == 1 && dt.day == 1) {
            ValueWriter.writeTime(stream, dt.hour, dt.minute, dt.second, 0, false, true);
            ValueWriter.writeNano(stream, dt.nanosecond);
        }
        else {
            ValueWriter.writeDate(stream, dt.year, dt.month, dt.day);
            if (dt.nanosecond == 0) {
                ValueWriter.writeTime(stream, dt.hour, dt.minute, dt.second, 0, true, true);
            }
            else {
                ValueWriter.writeTime(stream, dt.hour, dt.minute, dt.second, 0, false, true);
                ValueWriter.writeNano(stream, dt.nanosecond);
            }
        }
        stream.write(dt.utc ? TagUTC : TagSemicolon);
    }
}
