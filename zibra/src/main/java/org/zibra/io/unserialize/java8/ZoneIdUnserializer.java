package org.zibra.io.unserialize.java8;

import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZoneId;

public final class ZoneIdUnserializer extends BaseUnserializer<ZoneId> {

    public final static ZoneIdUnserializer instance = new ZoneIdUnserializer();

    @Override
    public ZoneId unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagString) return ZoneId.of(ReferenceReader.readString(reader));
        if (tag == TagEmpty) return null;
        return super.unserialize(reader, tag, type);
    }

    public ZoneId read(Reader reader) throws IOException {
        return read(reader, ZoneId.class);
    }
}
