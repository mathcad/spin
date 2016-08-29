package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public final class BigDecimalUnserializer  extends BaseUnserializer<BigDecimal> {

    public final static BigDecimalUnserializer instance = new BigDecimalUnserializer();

    @Override
    public BigDecimal unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag >= '0' && tag <= '9') return BigDecimal.valueOf(tag - '0');
        switch (tag) {
            case TagInteger:
            case TagLong:
            case TagDouble: return new BigDecimal(ValueReader.readUntil(reader, TagSemicolon).toString());
            case TagEmpty: return BigDecimal.ZERO;
            case TagTrue: return BigDecimal.ONE;
            case TagFalse: return BigDecimal.ZERO;
            case TagUTF8Char: return new BigDecimal(ValueReader.readUTF8Char(reader));
            case TagString: return new BigDecimal(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }
    public BigDecimal read(Reader reader) throws IOException {
       return read(reader, BigDecimal.class);
    }
}
