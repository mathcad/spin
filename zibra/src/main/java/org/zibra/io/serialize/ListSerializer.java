package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

public final class ListSerializer<T> extends ReferenceSerializer<List<T>> {

    public final static ListSerializer instance = new ListSerializer();

    @Override
    public final void serialize(Writer writer, List<T> list) throws IOException {
        super.serialize(writer, list);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int count = list.size();
        if (count > 0) {
            ValueWriter.writeInt(stream, count);
        }
        stream.write(TagOpenbrace);
        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                writer.serialize(list.get(i));
            }
        }
        else {
            for (Iterator<T> i = list.iterator(); i.hasNext();) {
                writer.serialize(i.next());
            }
        }
        stream.write(TagClosebrace);
    }
}
