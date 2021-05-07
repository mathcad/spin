package org.spin.core.inspection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于字节数组的类加载器
 */
public class BytesClassLoader extends ClassLoader {
    private static final ConcurrentHashMap<String, Class<?>> DEFIENED_CLASS = new ConcurrentHashMap<>();

    public BytesClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> defineClass(String name, final byte[] b) {
        DEFIENED_CLASS.computeIfAbsent(name, k -> defineClass(k, b, 0, b.length));
        return DEFIENED_CLASS.get(name);
    }

    public Class<?> findClassByName(String clazzName) {
        try {
            return getParent().loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }
}
