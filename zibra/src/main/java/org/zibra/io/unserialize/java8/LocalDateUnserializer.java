package org.zibra.io.unserialize.java8;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import org.zibra.io.convert.java8.LocalDateConverter;
import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import org.zibra.io.unserialize.ValueReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;

public final class LocalDateUnserializer extends BaseUnserializer<LocalDate> {

    public final static LocalDateUnserializer instance = new LocalDateUnserializer();

    @Override
    public LocalDate unserialize(Reader reader, int tag, Type type) throws IOException {
        LocalDateConverter converter = LocalDateConverter.instance;
        switch (tag) {
            case TagDate: return converter.convertTo(ReferenceReader.readDateTime(reader));
            case TagTime:  return converter.convertTo(ReferenceReader.readTime(reader));
            case TagEmpty: return null;
            case TagString: return converter.convertTo(ReferenceReader.readString(reader));
            case TagInteger:
            case TagLong: return converter.convertTo(ValueReader.readLong(reader));
            case TagDouble: return converter.convertTo(ValueReader.readDouble(reader));
        }
        if (tag >= '0' && tag <= '9') return converter.convertTo(tag - '0');
        return super.unserialize(reader, tag, type);
    }

    public LocalDate read(Reader reader) throws IOException {
        return read(reader, LocalDate.class);
    }
}
