package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

public final class BigIntegerUnserializer extends BaseUnserializer<BigInteger> {

    public final static BigIntegerUnserializer instance = new BigIntegerUnserializer();

    @Override
    public BigInteger unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag >= '0' && tag <= '9') return BigInteger.valueOf(tag - '0');
        switch (tag) {
            case TagInteger:
            case TagLong:
            case TagDouble: return new BigInteger(ValueReader.readUntil(reader, TagSemicolon).toString());
            case TagEmpty: return BigInteger.ZERO;
            case TagTrue: return BigInteger.ONE;
            case TagFalse: return BigInteger.ZERO;
            case TagDate: return ReferenceReader.readDateTime(reader).toBigInteger();
            case TagTime: return ReferenceReader.readTime(reader).toBigInteger();
            case TagUTF8Char: return new BigInteger(ValueReader.readUTF8Char(reader));
            case TagString: return new BigInteger(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public BigInteger read(Reader reader) throws IOException {
       return read(reader, BigInteger.class);
    }
}
