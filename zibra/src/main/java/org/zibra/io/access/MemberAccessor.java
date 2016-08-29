package org.zibra.io.access;

import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.Reader;
import java.io.IOException;

public interface MemberAccessor {
    void serialize(Writer writer, Object obj) throws IOException;
    void unserialize(Reader reader, Object obj) throws IOException;
}