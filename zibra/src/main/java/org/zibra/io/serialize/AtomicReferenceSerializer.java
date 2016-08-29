package org.zibra.io.serialize;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class AtomicReferenceSerializer implements Serializer<AtomicReference> {

    public final static AtomicReferenceSerializer instance = new AtomicReferenceSerializer();

    public final void write(Writer writer, AtomicReference obj) throws IOException {
        writer.serialize(obj.get());
    }
}
