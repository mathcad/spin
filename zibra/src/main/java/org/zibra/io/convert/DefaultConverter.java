package org.zibra.io.convert;

import org.zibra.util.ClassUtil;
import java.lang.reflect.Type;

public class DefaultConverter implements Converter {

    public final static DefaultConverter instance = new DefaultConverter();

    public Object convertTo(Object obj, Type type) {
        if (type == null) return obj;
        Class<?> cls = ClassUtil.toClass(type);
        if (cls == null || cls.equals(Object.class) || cls.isInstance(obj)) {
            return obj;
        }
        Converter converter = ConverterFactory.get(cls);
        if (converter == null) {
            return cls.cast(obj);
        }
        return converter.convertTo(obj, type);
    }
}
