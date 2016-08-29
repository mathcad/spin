package org.zibra.io.serialize;

import java.io.IOException;

public final class EnumSerializer implements Serializer<Enum> {

    public final static EnumSerializer instance = new EnumSerializer();

    public final void write(Writer writer, Enum obj) throws IOException {
        ValueWriter.write(writer.stream, obj.ordinal());
    }
}
