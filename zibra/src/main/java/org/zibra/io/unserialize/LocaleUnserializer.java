package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import org.zibra.io.convert.LocaleConverter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;

public final class LocaleUnserializer extends BaseUnserializer<Locale> {

    public final static LocaleUnserializer instance = new LocaleUnserializer();

    @Override
    public Locale unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagEmpty) return null;
        if (tag == TagString) {
            String str = ReferenceReader.readString(reader);
            return LocaleConverter.instance.convertTo(str);
        }
        return super.unserialize(reader, tag, type);
    }

    public Locale read(Reader reader) throws IOException {
        return read(reader, Locale.class);
    }
}
