package org.zibra.io.serialize;

import org.zibra.common.ZibraException;

import java.io.IOException;

public final class ObjectSerializer implements Serializer {

    public final static ObjectSerializer instance = new ObjectSerializer();

    public final void write(Writer writer, Object obj) throws IOException {
        if (obj != null) {
            Class<?> cls = obj.getClass();
            if (Object.class.equals(cls)) {
                throw new ZibraException("Can't serialize an object of the Object class.");
            }
        }
        writer.serialize(obj);
    }
}
