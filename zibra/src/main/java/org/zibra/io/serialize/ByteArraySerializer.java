package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagBytes;
import static org.zibra.io.Tags.TagQuote;
import java.io.IOException;
import java.io.OutputStream;

public final class ByteArraySerializer extends ReferenceSerializer<byte[]> {

    public final static ByteArraySerializer instance = new ByteArraySerializer();

    @Override
    public final void serialize(Writer writer, byte[] bytes) throws IOException {
        super.serialize(writer, bytes);
        OutputStream stream = writer.stream;
        stream.write(TagBytes);
        int length = bytes.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagQuote);
        stream.write(bytes);
        stream.write(TagQuote);
    }
}
