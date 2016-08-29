package org.zibra.io.unserialize.java8;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import org.zibra.io.convert.java8.LocalDateTimeConverter;
import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public final class LocalDateTimeUnserializer extends BaseUnserializer<LocalDateTime> {

    public final static LocalDateTimeUnserializer instance = new LocalDateTimeUnserializer();

    @Override
    @SuppressWarnings({"deprecation"})
    public LocalDateTime unserialize(Reader reader, int tag, Type type) throws IOException {
        LocalDateTimeConverter converter = LocalDateTimeConverter.instance;
        switch (tag) {
            case TagDate: return converter.convertTo(ReferenceReader.readDateTime(reader));
            case TagTime:  return converter.convertTo(ReferenceReader.readTime(reader));
            case TagEmpty: return null;
            case TagString: return converter.convertTo(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public LocalDateTime read(Reader reader) throws IOException {
        return read(reader, LocalDateTime.class);
    }
}
