package org.zibra.io.convert;

import java.lang.reflect.Type;

public interface Converter<T> {
    T convertTo(Object obj, Type type);
}
