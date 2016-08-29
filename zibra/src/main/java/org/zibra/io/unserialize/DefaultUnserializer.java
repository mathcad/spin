package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagBytes;
import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagGuid;
import static org.zibra.io.Tags.TagInfinity;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagNaN;
import static org.zibra.io.Tags.TagObject;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;


public final class DefaultUnserializer extends BaseUnserializer {

    public final static DefaultUnserializer instance = new DefaultUnserializer();

    @Override
    public Object unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case TagEmpty: return "";
            case TagTrue: return true;
            case TagFalse: return false;
            case TagNaN: return Double.NaN;
            case TagInteger: return ValueReader.readInt(reader);
            case TagLong: return ValueReader.readBigInteger(reader);
            case TagDouble: return ValueReader.readDouble(reader);
            case TagInfinity: return ValueReader.readInfinity(reader);
            case TagUTF8Char: return ValueReader.readUTF8Char(reader);
            case TagString: return ReferenceReader.readString(reader);
            case TagBytes: return ReferenceReader.readBytes(reader);
            case TagDate: return ReferenceReader.readDateTime(reader).toCalendar();
            case TagTime: return ReferenceReader.readTime(reader).toCalendar();
            case TagGuid: return ReferenceReader.readUUID(reader);
            case TagList: return ReferenceReader.readArrayList(reader);
            case TagMap: return ReferenceReader.readHashMap(reader);
            case TagObject: return ReferenceReader.readObject(reader);
        }
        return super.unserialize(reader, tag, type);
    }

    public Object read(Reader reader) throws IOException {
        return read(reader, Object.class);
    }
}
