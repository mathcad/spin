package org.zibra.io.unserialize.java8;

import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZoneOffset;

public final class ZoneOffsetUnserializer extends BaseUnserializer<ZoneOffset> {

    public final static ZoneOffsetUnserializer instance = new ZoneOffsetUnserializer();

    @Override
    public ZoneOffset unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagString) return ZoneOffset.of(ReferenceReader.readString(reader));
        if (tag == TagEmpty) return null;
        return super.unserialize(reader, tag, type);
    }

    public ZoneOffset read(Reader reader) throws IOException {
        return read(reader, ZoneOffset.class);
    }
}
