package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;

public final class HashSetUnserializer extends BaseUnserializer<HashSet> {

    public final static HashSetUnserializer instance = new HashSetUnserializer();

    @Override
    public HashSet unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readHashSet(reader, type);
        return super.unserialize(reader, tag, type);
    }

    public HashSet read(Reader reader) throws IOException {
        return super.read(reader, HashSet.class);
    }
}
