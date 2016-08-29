package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapUnserializer extends BaseUnserializer<Map> {

    public final static MapUnserializer instance = new MapUnserializer();

    @Override
    public Map unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagList: return ReferenceReader.readListAsMap(reader, type);
            case TagMap: return ReferenceReader.readMap(reader, type);
            case TagObject:  return ReferenceReader.readObjectAsMap(reader, type);
        }
        return super.unserialize(reader, tag, type);
    }

    public Map read(Reader reader) throws IOException {
        return read(reader, LinkedHashMap.class);
    }
}
