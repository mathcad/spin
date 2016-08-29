package org.zibra.io.unserialize.java8;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import org.zibra.io.convert.java8.ZonedDateTimeConverter;
import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;

public final class ZonedDateTimeUnserializer extends BaseUnserializer<ZonedDateTime> {

    public final static ZonedDateTimeUnserializer instance = new ZonedDateTimeUnserializer();

    @Override
    public ZonedDateTime unserialize(Reader reader, int tag, Type type) throws IOException {
        ZonedDateTimeConverter converter = ZonedDateTimeConverter.instance;
        switch (tag) {
            case TagString: return ZonedDateTime.parse(ReferenceReader.readString(reader));
            case TagDate: return converter.convertTo(ReferenceReader.readDateTime(reader));
            case TagTime: return converter.convertTo(ReferenceReader.readTime(reader));
            case TagEmpty: return null;
        }
        return super.unserialize(reader, tag, type);
    }

    public ZonedDateTime read(Reader reader) throws IOException {
        return read(reader, ZonedDateTime.class);
    }
}
