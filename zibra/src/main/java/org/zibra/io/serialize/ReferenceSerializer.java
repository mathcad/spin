package org.zibra.io.serialize;

import java.io.IOException;

public abstract class ReferenceSerializer<T> implements Serializer<T> {

    public void serialize(Writer writer, T obj) throws IOException {
        writer.setRef(obj);
        // write your actual serialization code in sub class
    }

    public void write(Writer writer, T obj) throws IOException {
        if (!writer.writeRef(obj)) {
            serialize(writer, obj);
        }
    }
}
