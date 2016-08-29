package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public final class MapSerializer<K, V> extends ReferenceSerializer<Map<K, V>> {

    public final static MapSerializer instance = new MapSerializer();

    @Override
    public final void serialize(Writer writer, Map<K, V> map) throws IOException {
        super.serialize(writer, map);
        OutputStream stream = writer.stream;
        int count = map.size();
        stream.write(TagMap);
        if (count > 0) {
            ValueWriter.writeInt(stream, count);
        }
        stream.write(TagOpenbrace);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            writer.serialize(entry.getKey());
            writer.serialize(entry.getValue());
        }
        stream.write(TagClosebrace);
    }
}
