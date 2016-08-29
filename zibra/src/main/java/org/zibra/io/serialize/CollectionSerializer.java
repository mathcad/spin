package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

public final class CollectionSerializer<T> extends ReferenceSerializer<Collection<T>> {

    public final static CollectionSerializer instance = new CollectionSerializer();

    @Override
    public final void serialize(Writer writer, Collection<T> collection) throws IOException {
        super.serialize(writer, collection);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int count = collection.size();
        if (count > 0) {
            ValueWriter.writeInt(stream, count);
        }
        stream.write(TagOpenbrace);
        for (T aCollection : collection) {
            writer.serialize(aCollection);
        }
        stream.write(TagClosebrace);
    }
}
