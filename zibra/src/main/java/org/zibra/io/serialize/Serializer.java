package org.zibra.io.serialize;

import java.io.IOException;

public interface Serializer<T> {
    void write(Writer writer, T obj) throws IOException;
}
