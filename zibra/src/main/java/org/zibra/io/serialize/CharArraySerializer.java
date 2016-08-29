package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.io.OutputStream;

public final class CharArraySerializer extends ReferenceSerializer<char[]> {

    public final static CharArraySerializer instance = new CharArraySerializer();

    @Override
    public final void serialize(Writer writer, char[] s) throws IOException {
        super.serialize(writer, s);
        OutputStream stream = writer.stream;
        stream.write(TagString);
        ValueWriter.write(stream, s);
    }

    @Override
    public final void write(Writer writer, char[] obj) throws IOException {
        OutputStream stream = writer.stream;
        switch (obj.length) {
            case 0:
                stream.write(TagEmpty);
                break;
            case 1:
                ValueWriter.write(stream, obj[0]);
                break;
            default:
                super.write(writer, obj);
                break;
        }
    }
}
