package org.zibra.io.serialize;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AtomicBooleanSerializer implements Serializer<AtomicBoolean> {

    public final static AtomicBooleanSerializer instance = new AtomicBooleanSerializer();

    public final void write(Writer writer, AtomicBoolean obj) throws IOException {
        ValueWriter.write(writer.stream, obj.get());
    }
}
