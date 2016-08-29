package org.zibra.io.convert;

import org.zibra.util.DateTime;
import java.lang.reflect.Type;
import java.math.BigInteger;

public class BigIntegerConverter implements Converter<BigInteger> {

    public final static BigIntegerConverter instance = new BigIntegerConverter();

    public BigInteger convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return new BigInteger((String) obj);
        }
        else if (obj instanceof char[]) {
            return new BigInteger(new String((char[]) obj));
        }
        else if (obj instanceof DateTime) {
            return ((DateTime) obj).toBigInteger();
        }
        return (BigInteger) obj;
    }
}
