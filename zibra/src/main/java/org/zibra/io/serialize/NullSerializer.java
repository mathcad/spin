package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;

public final class NullSerializer implements Serializer {

    public final static NullSerializer instance = new NullSerializer();

    public void write(Writer writer, Object obj) throws IOException {
        writer.stream.write(TagNull);
    }
}
