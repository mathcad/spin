package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public final class BigDecimalArrayUnserializer extends BaseUnserializer<BigDecimal[]> {

    public final static BigDecimalArrayUnserializer instance = new BigDecimalArrayUnserializer();

    @Override
    public BigDecimal[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readBigDecimalArray(reader);
        if (tag == TagEmpty) return new BigDecimal[0];
        return super.unserialize(reader, tag, type);
    }

    public BigDecimal[] read(Reader reader) throws IOException {
       return read(reader, BigDecimal[].class);
    }
}
