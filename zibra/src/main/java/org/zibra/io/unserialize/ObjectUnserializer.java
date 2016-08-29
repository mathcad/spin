package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagObject;
import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectUnserializer  extends BaseUnserializer {

    public final static ObjectUnserializer instance = new ObjectUnserializer();

    @Override
    public Object unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagEmpty: return null;
            case TagMap: return ReferenceReader.readMapAsObject(reader, type);
            case TagObject: return ReferenceReader.readObject(reader, type);
        }
        return super.unserialize(reader, tag, type);
    }

    public Object read(Reader reader) throws IOException {
        return read(reader, Object.class);
    }
}
