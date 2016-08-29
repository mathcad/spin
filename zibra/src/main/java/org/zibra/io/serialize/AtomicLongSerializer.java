package org.zibra.io.serialize;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public final class AtomicLongSerializer implements Serializer<AtomicLong> {

    public final static AtomicLongSerializer instance = new AtomicLongSerializer();

    public final void write(Writer writer, AtomicLong obj) throws IOException {
        ValueWriter.write(writer.stream, obj.get());
    }
}
