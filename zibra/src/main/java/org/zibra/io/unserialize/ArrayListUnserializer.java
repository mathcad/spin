package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public final class ArrayListUnserializer extends BaseUnserializer<ArrayList> {

    public final static ArrayListUnserializer instance = new ArrayListUnserializer();

    @Override
    public ArrayList unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readArrayList(reader, type);
        return super.unserialize(reader, tag, type);
    }

    public ArrayList read(Reader reader) throws IOException {
       return read(reader, ArrayList.class);
    }
}
