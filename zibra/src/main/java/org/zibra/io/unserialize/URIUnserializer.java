package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import org.zibra.io.convert.URIConverter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;

public final class URIUnserializer extends BaseUnserializer<URI> {

    public final static URIUnserializer instance = new URIUnserializer();

    @Override
    public URI unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagString) {
            String str = ReferenceReader.readString(reader);
            return URIConverter.instance.convertTo(str, URI.class);
        }
        if (tag == TagEmpty) return null;
        return super.unserialize(reader, tag, type);
    }

    public URI read(Reader reader) throws IOException {
        return read(reader, URI.class);
    }
}
