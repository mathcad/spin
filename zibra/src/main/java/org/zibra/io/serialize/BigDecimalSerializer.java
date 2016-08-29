package org.zibra.io.serialize;

import java.io.IOException;
import java.math.BigDecimal;

public final class BigDecimalSerializer implements Serializer<BigDecimal> {

    public final static BigDecimalSerializer instance = new BigDecimalSerializer();

    public final void write(Writer writer, BigDecimal obj) throws IOException {
        ValueWriter.write(writer.stream, obj);
    }
}
