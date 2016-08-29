package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class FloatArrayUnserializer extends BaseUnserializer<float[]> {

    public final static FloatArrayUnserializer instance = new FloatArrayUnserializer();

    @Override
    public float[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readFloatArray(reader);
        if (tag == TagEmpty) return new float[0];
        return super.unserialize(reader, tag, type);
    }

    public float[] read(Reader reader) throws IOException {
        return read(reader, float[].class);
    }
}
