package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

public final class BigIntegerArraySerializer extends ReferenceSerializer<BigInteger[]> {

    public final static BigIntegerArraySerializer instance = new BigIntegerArraySerializer();

    @Override
    public final void serialize(Writer writer, BigInteger[] array) throws IOException {
        super.serialize(writer, array);
        OutputStream stream = writer.stream;
        stream.write(TagList);
        int length = array.length;
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            BigInteger e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else {
                ValueWriter.write(stream, e);
            }
        }
        stream.write(TagClosebrace);
    }
}
