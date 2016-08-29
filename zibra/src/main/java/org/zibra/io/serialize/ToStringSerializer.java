package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.io.OutputStream;

public final class ToStringSerializer extends ReferenceSerializer {

    public final static ToStringSerializer instance = new ToStringSerializer();

    @Override
    @SuppressWarnings({"unchecked"})
    public final void serialize(Writer writer, Object obj) throws IOException {
        super.serialize(writer, obj);
        OutputStream stream = writer.stream;
        stream.write(TagString);
        ValueWriter.write(stream, obj.toString());
    }
}
