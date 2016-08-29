package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class BigIntegerArrayUnserializer extends BaseUnserializer<BigInteger[]> {

    public final static BigIntegerArrayUnserializer instance = new BigIntegerArrayUnserializer();

    @Override
    public BigInteger[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readBigIntegerArray(reader);
        if (tag == TagEmpty) return new BigInteger[0];
        return super.unserialize(reader, tag, type);
    }

    public BigInteger[] read(Reader reader) throws IOException {
       return read(reader, BigInteger[].class);
    }
}
