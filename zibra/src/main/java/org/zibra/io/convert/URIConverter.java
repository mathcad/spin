package org.zibra.io.convert;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

public class URIConverter implements Converter<URI> {

    public final static URIConverter instance = new URIConverter();

    private static URI convertTo(String s) {
        try {
            return new URI(s);
        }
        catch (URISyntaxException e) {
            throw new ClassCastException("String \"" + s + "\" cannot be cast to java.net.URI");
        }
    }

    public URI convertTo(Object obj, Type type) {
        if (obj instanceof String) {
            return convertTo((String) obj);
        }
        else if (obj instanceof char[]) {
            return convertTo(new String((char[]) obj));
        }
        return (URI) obj;
    }
}
