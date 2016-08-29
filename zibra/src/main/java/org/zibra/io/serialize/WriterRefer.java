package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagRef;
import static org.zibra.io.Tags.TagSemicolon;
import java.io.IOException;
import java.io.OutputStream;
import java.util.IdentityHashMap;

final class WriterRefer {
    private final IdentityHashMap<Object, Integer> ref = new IdentityHashMap<>();
    private int lastref = 0;
    public final void addCount(int count) {
        lastref += count;
    }
    public final void set(Object obj) {
        ref.put(obj, lastref++);
    }
    public final boolean write(OutputStream stream, Object obj) throws IOException {
        Integer r = ref.get(obj);
        if (r != null) {
            stream.write(TagRef);
            ValueWriter.writeInt(stream, r);
            stream.write(TagSemicolon);
            return true;
        }
        return false;
    }
    public final void reset() {
        ref.clear();
        lastref = 0;
    }
}