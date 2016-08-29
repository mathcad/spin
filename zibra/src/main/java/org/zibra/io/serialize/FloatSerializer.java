package org.zibra.io.serialize;

import java.io.IOException;

public final class FloatSerializer implements Serializer<Float> {

    public final static FloatSerializer instance = new FloatSerializer();

    public final void write(Writer writer, Float obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
