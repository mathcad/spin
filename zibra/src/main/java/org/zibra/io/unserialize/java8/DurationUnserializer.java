package org.zibra.io.unserialize.java8;

import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import org.zibra.io.unserialize.ValueReader;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;

public final class DurationUnserializer extends BaseUnserializer<Duration> {

    public final static DurationUnserializer instance = new DurationUnserializer();

    @Override
    public Duration unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagEmpty: return null;
            case TagString: return Duration.parse(ReferenceReader.readString(reader));
            case TagInteger:
            case TagLong: return Duration.ofNanos(ValueReader.readLong(reader));
            case TagDouble: return Duration.ofNanos((long)ValueReader.readDouble(reader));
        }
        if (tag >= '0' && tag <= '9') return Duration.ofNanos(tag - '0');
        return super.unserialize(reader, tag, type);
    }

    public Duration read(Reader reader) throws IOException {
        return read(reader, Duration.class);
    }
}
