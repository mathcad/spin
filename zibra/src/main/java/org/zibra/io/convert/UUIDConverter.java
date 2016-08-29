package org.zibra.io.convert;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDConverter implements Converter<UUID> {

    public final static UUIDConverter instance = new UUIDConverter();

    public UUID convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return UUID.fromString((String) obj);
        }
        else if (obj instanceof char[]) {
            return UUID.fromString(new String((char[]) obj));
        }
        else if (obj instanceof byte[]) {
            return UUID.nameUUIDFromBytes((byte[]) obj);
        }
        return (UUID) obj;
    }
}
