package org.zibra.io.serialize;

import java.io.IOException;

public final class CharSerializer implements Serializer<Character> {

    public final static CharSerializer instance = new CharSerializer();

    public final void write(Writer writer, Character obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
