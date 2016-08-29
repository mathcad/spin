package org.zibra.io.serialize;

import java.io.IOException;

public final class ByteSerializer implements Serializer<Byte> {

    public final static ByteSerializer instance = new ByteSerializer();

    public final void write(Writer writer, Byte obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
