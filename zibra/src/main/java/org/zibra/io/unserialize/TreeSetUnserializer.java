package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.TreeSet;

public final class TreeSetUnserializer extends BaseUnserializer<TreeSet> {

    public final static TreeSetUnserializer instance = new TreeSetUnserializer();

    @Override
    public TreeSet unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readTreeSet(reader, type);
        return super.unserialize(reader, tag, type);
    }

    public TreeSet read(Reader reader) throws IOException {
        return read(reader, TreeSet.class);
    }
}
