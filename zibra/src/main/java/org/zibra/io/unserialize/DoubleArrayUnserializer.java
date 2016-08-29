package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class DoubleArrayUnserializer extends BaseUnserializer<double[]> {

    public final static DoubleArrayUnserializer instance = new DoubleArrayUnserializer();

    @Override
    public double[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readDoubleArray(reader);
        if (tag == TagEmpty) return new double[0];
        return super.unserialize(reader, tag, type);
    }

    public double[] read(Reader reader) throws IOException {
        return read(reader, double[].class);
    }
}
