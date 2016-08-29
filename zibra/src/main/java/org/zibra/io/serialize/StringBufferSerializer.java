package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.io.OutputStream;

public final class StringBufferSerializer extends ReferenceSerializer<StringBuffer> {

    public final static StringBufferSerializer instance = new StringBufferSerializer();

    @Override
    public final void serialize(Writer writer, StringBuffer s) throws IOException {
        super.serialize(writer, s);
        OutputStream stream = writer.stream;
        stream.write(TagString);
        ValueWriter.write(stream, s.toString());
    }

    @Override
    public final void write(Writer writer, StringBuffer obj) throws IOException {
        OutputStream stream = writer.stream;
        switch (obj.length()) {
            case 0:
                stream.write(TagEmpty);
                break;
            case 1:
                ValueWriter.write(stream, obj.charAt(0));
                break;
            default:
                super.write(writer, obj);
                break;
        }
    }
}
