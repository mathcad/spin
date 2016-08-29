package org.zibra.io.serialize;

import java.io.IOException;

public final class ShortSerializer implements Serializer<Short> {

    public final static ShortSerializer instance = new ShortSerializer();

    public final void write(Writer writer, Short obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
