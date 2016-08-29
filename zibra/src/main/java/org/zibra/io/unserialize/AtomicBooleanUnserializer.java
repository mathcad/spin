package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AtomicBooleanUnserializer implements Unserializer<AtomicBoolean> {

    public final static AtomicBooleanUnserializer instance = new AtomicBooleanUnserializer();

    public AtomicBoolean read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return null;
        return new AtomicBoolean(BooleanObjectUnserializer.instance.read(reader, tag, Boolean.class));
    }

    public AtomicBoolean read(Reader reader, Type type) throws IOException {
       return read(reader, reader.stream.read(), type);
    }

    public AtomicBoolean read(Reader reader) throws IOException {
       return read(reader, AtomicBoolean.class);
    }
}
