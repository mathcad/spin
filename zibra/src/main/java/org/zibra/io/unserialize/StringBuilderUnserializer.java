package org.zibra.io.unserialize;

import org.zibra.io.convert.StringBuilderConverter;
import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagGuid;
import static org.zibra.io.Tags.TagInfinity;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagNaN;
import static org.zibra.io.Tags.TagPos;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public final class StringBuilderUnserializer extends BaseUnserializer<StringBuilder> {

    public final static StringBuilderUnserializer instance = new StringBuilderUnserializer();

    @Override
    public StringBuilder unserialize(Reader reader, int tag, Type type) throws IOException {
        StringBuilderConverter converter = StringBuilderConverter.instance;
        switch (tag) {
            case TagEmpty: return new StringBuilder();
            case TagString: return converter.convertTo(ReferenceReader.readChars(reader));
            case TagUTF8Char: return new StringBuilder().append(ValueReader.readChar(reader));
            case TagInteger: return ValueReader.readUntil(reader, TagSemicolon);
            case TagLong: return ValueReader.readUntil(reader, TagSemicolon);
            case TagDouble: return ValueReader.readUntil(reader, TagSemicolon);
        }
        if (tag >= '0' && tag <= '9') return new StringBuilder().append((char) tag);
        switch (tag) {
            case TagTrue: return new StringBuilder("true");
            case TagFalse: return new StringBuilder("false");
            case TagNaN: return new StringBuilder("NaN");
            case TagInfinity: return new StringBuilder((reader.stream.read() == TagPos) ?
                                                 "Infinity" : "-Infinity");
            case TagDate: return ReferenceReader.readDateTime(reader).toStringBuilder();
            case TagTime: return ReferenceReader.readTime(reader).toStringBuilder();
            case TagGuid: return new StringBuilder(ReferenceReader.readUUID(reader).toString());
        }
        return super.unserialize(reader, tag, type);
    }

    public StringBuilder read(Reader reader) throws IOException {
        return read(reader, StringBuilder.class);
    }
}
