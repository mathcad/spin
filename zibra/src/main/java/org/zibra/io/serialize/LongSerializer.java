package org.zibra.io.serialize;

import java.io.IOException;

public final class LongSerializer implements Serializer<Long> {

    public final static LongSerializer instance = new LongSerializer();

    public final void write(Writer writer, Long obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
