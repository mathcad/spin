package org.zibra.io.unserialize;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Unserializer<T> {
    T read(Reader reader, int tag, Type type) throws IOException;
}
