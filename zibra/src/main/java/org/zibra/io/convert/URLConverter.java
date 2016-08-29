package org.zibra.io.convert;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

public class URLConverter implements Converter<URL> {

    public final static URLConverter instance = new URLConverter();

    private static URL convertTo(String s) {
        try {
            return new URL(s);
        }
        catch (MalformedURLException e) {
            throw new ClassCastException("String \"" + s + "\" cannot be cast to java.net.URL");
        }
    }

    public URL convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return convertTo((String) obj);
        }
        else if (obj instanceof char[]) {
            return convertTo(new String((char[]) obj));
        }
        return (URL) obj;
    }
}
