package org.zibra.io.serialize;

import java.io.IOException;

public final class DoubleSerializer implements Serializer<Double> {

    public final static DoubleSerializer instance = new DoubleSerializer();

    public final void write(Writer writer, Double obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
