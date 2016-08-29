package org.zibra.io.serialize;

import java.io.IOException;

public final class IntegerSerializer implements Serializer<Integer> {

    public final static IntegerSerializer instance = new IntegerSerializer();

    public final void write(Writer writer, Integer obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
