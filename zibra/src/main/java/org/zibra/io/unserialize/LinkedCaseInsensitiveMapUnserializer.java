package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagObject;
import org.zibra.util.LinkedCaseInsensitiveMap;
import java.io.IOException;
import java.lang.reflect.Type;

public final class LinkedCaseInsensitiveMapUnserializer extends BaseUnserializer<LinkedCaseInsensitiveMap> {

    public final static LinkedCaseInsensitiveMapUnserializer instance = new LinkedCaseInsensitiveMapUnserializer();

    @Override
    public LinkedCaseInsensitiveMap unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagList: return ReferenceReader.readListAsLinkedCaseInsensitiveMap(reader, type);
            case TagMap: return ReferenceReader.readLinkedCaseInsensitiveMap(reader, type);
            case TagObject: return ReferenceReader.readObjectAsLinkedCaseInsensitiveMap(reader, type);
        }

        return super.unserialize(reader, tag, type);
    }

    public LinkedCaseInsensitiveMap read(Reader reader) throws IOException {
        return read(reader, LinkedCaseInsensitiveMap.class);
    }
}
