package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagObject;
import org.zibra.util.CaseInsensitiveMap;
import java.io.IOException;
import java.lang.reflect.Type;

public final class CaseInsensitiveMapUnserializer extends BaseUnserializer<CaseInsensitiveMap> {

    public final static CaseInsensitiveMapUnserializer instance = new CaseInsensitiveMapUnserializer();

    @Override
    public CaseInsensitiveMap unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagList: return ReferenceReader.readListAsCaseInsensitiveMap(reader, type);
            case TagMap: return ReferenceReader.readCaseInsensitiveMap(reader, type);
            case TagObject:  return ReferenceReader.readObjectAsCaseInsensitiveMap(reader, type);
        }

        return super.unserialize(reader, tag, type);
    }

    public CaseInsensitiveMap read(Reader reader) throws IOException {
        return read(reader, CaseInsensitiveMap.class);
    }
}
