package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;

public final class LinkedListUnserializer extends BaseUnserializer<LinkedList> {

    public final static LinkedListUnserializer instance = new LinkedListUnserializer();

    @Override
    public LinkedList unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readLinkedList(reader, type);
        return super.unserialize(reader, tag, type);
    }

    public LinkedList read(Reader reader) throws IOException {
        return read(reader, LinkedList.class);
    }
}
