package org.zibra.io.serialize;

import java.io.IOException;

public final class BooleanSerializer implements Serializer<Boolean> {

    public final static BooleanSerializer instance = new BooleanSerializer();

    public final void write(Writer writer, Boolean obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
