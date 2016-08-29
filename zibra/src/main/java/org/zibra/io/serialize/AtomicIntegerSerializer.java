package org.zibra.io.serialize;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public final class AtomicIntegerSerializer implements Serializer<AtomicInteger> {

    public final static AtomicIntegerSerializer instance = new AtomicIntegerSerializer();

    public final void write(Writer writer, AtomicInteger obj) throws IOException {
        ValueWriter.write(writer.stream, obj.get());
    }
}
