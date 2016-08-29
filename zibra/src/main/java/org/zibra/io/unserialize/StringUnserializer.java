package org.zibra.io.unserialize;

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

public final class StringUnserializer extends BaseUnserializer<String> {

    public final static StringUnserializer instance = new StringUnserializer();

    @Override
    public String unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagEmpty: return "";
            case TagString: return ReferenceReader.readString(reader);
            case TagUTF8Char: return ValueReader.readUTF8Char(reader);
            case TagInteger: return ValueReader.readUntil(reader, TagSemicolon).toString();
            case TagLong: return ValueReader.readUntil(reader, TagSemicolon).toString();
            case TagDouble: return ValueReader.readUntil(reader, TagSemicolon).toString();
        }
        if (tag >= '0' && tag <= '9') return String.valueOf((char) tag);
        switch (tag) {
            case TagTrue: return "true";
            case TagFalse: return "false";
            case TagNaN: return "NaN";
            case TagInfinity: return (reader.stream.read() == TagPos) ?
                                                 "Infinity" : "-Infinity";
            case TagDate: return ReferenceReader.readDateTime(reader).toString();
            case TagTime: return ReferenceReader.readTime(reader).toString();
            case TagGuid: return ReferenceReader.readUUID(reader).toString();
        }
        return super.unserialize(reader, tag, type);
    }

    public String read(Reader reader) throws IOException {
        return read(reader, String.class);
    }
}
