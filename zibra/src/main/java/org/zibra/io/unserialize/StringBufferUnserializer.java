package org.zibra.io.unserialize;

import org.zibra.io.convert.StringBufferConverter;
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

public final class StringBufferUnserializer extends BaseUnserializer<StringBuffer> {

    public final static StringBufferUnserializer instance = new StringBufferUnserializer();

    @Override
    public StringBuffer unserialize(Reader reader, int tag, Type type) throws IOException {
        StringBufferConverter converter = StringBufferConverter.instance;
        switch (tag) {
            case TagEmpty: return new StringBuffer();
            case TagString: return converter.convertTo(ReferenceReader.readChars(reader));
            case TagUTF8Char: return new StringBuffer().append(ValueReader.readChar(reader));
            case TagInteger: return new StringBuffer(ValueReader.readUntil(reader, TagSemicolon));
            case TagLong: return new StringBuffer(ValueReader.readUntil(reader, TagSemicolon));
            case TagDouble: return new StringBuffer(ValueReader.readUntil(reader, TagSemicolon));
        }
        if (tag >= '0' && tag <= '9') return new StringBuffer().append((char) tag);
        switch (tag) {
            case TagTrue: return new StringBuffer("true");
            case TagFalse: return new StringBuffer("false");
            case TagNaN: return new StringBuffer("NaN");
            case TagInfinity: return new StringBuffer((reader.stream.read() == TagPos) ?
                                                 "Infinity" : "-Infinity");
            case TagDate: return ReferenceReader.readDateTime(reader).toStringBuffer();
            case TagTime: return ReferenceReader.readTime(reader).toStringBuffer();
            case TagGuid: return new StringBuffer(ReferenceReader.readUUID(reader).toString());
        }
        return super.unserialize(reader, tag, type);
    }

    public StringBuffer read(Reader reader) throws IOException {
        return read(reader, StringBuffer.class);
    }
}
