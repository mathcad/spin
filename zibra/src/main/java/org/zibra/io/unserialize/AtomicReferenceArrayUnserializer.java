package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import org.zibra.util.ClassUtil;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class AtomicReferenceArrayUnserializer implements Unserializer<AtomicReferenceArray> {

    public final static AtomicReferenceArrayUnserializer instance = new AtomicReferenceArrayUnserializer();

    @SuppressWarnings({"unchecked"})
    public AtomicReferenceArray read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return null;
        type = ClassUtil.getComponentType(type);
        Object[] array = (Object[])ArrayUnserializer.instance.read(reader, tag, type);
        return new AtomicReferenceArray(array);
    }
}
