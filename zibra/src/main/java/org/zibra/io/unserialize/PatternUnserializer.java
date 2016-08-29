package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

public final class PatternUnserializer extends BaseUnserializer<Pattern> {

    public final static PatternUnserializer instance = new PatternUnserializer();

    @Override
    public Pattern unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagString: return Pattern.compile(ReferenceReader.readString(reader));
            case TagEmpty: return null;
        }
        return super.unserialize(reader, tag, type);
    }

    public Pattern read(Reader reader) throws IOException {
        return read(reader, Pattern.class);
    }
}
