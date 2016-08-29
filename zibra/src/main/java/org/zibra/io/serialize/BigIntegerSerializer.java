package org.zibra.io.serialize;

import java.io.IOException;
import java.math.BigInteger;

public final class BigIntegerSerializer implements Serializer<BigInteger> {

    public final static BigIntegerSerializer instance = new BigIntegerSerializer();

    public final void write(Writer writer, BigInteger obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
