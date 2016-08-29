package org.zibra.util;

import org.zibra.io.ZibraClassManager;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;

public final class ClassUtil {

    private static Class<?> getInnerClass(StringBuilder className, int[] pos, int i, char c) {
        if (i < pos.length) {
            int p = pos[i];
            className.setCharAt(p, c);
            Class<?> type = getInnerClass(className, pos, i + 1, '_');
            if (i + 1 < pos.length && type == null) {
                type = getInnerClass(className, pos, i + 1, '$');
            }
            return type;
        } else {
            try {
                return Class.forName(className.toString());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    public static String getClassAlias(Class<?> type) {
        String className = ZibraClassManager.getClassAlias(type);
        if (className == null) {
            className = type.getName().replace('.', '_').replace('$', '_');
            ZibraClassManager.register(type, className);
        }
        return className;
    }

    private static Class<?> getClass(StringBuilder className, int[] pos, int i, char c) {
        if (i < pos.length) {
            int p = pos[i];
            className.setCharAt(p, c);
            Class<?> type = getClass(className, pos, i + 1, '.');
            if (i + 1 < pos.length) {
                if (type == null) {
                    type = getClass(className, pos, i + 1, '_');
                }
                if (type == null) {
                    type = getInnerClass(className, pos, i + 1, '$');
                }
            }
            return type;
        } else {
            try {
                return Class.forName(className.toString());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    public static Class<?> getClass(String className) {
        Class<?> type = ZibraClassManager.getClass(className);
        if (type == null) {
            StringBuilder cn = new StringBuilder(className);
            ArrayList<Integer> al = new ArrayList<>();
            int p = cn.indexOf("_");
            while (p > -1) {
                al.add(p);
                p = cn.indexOf("_", p + 1);
            }
            if (al.size() > 0) {
                try {
                    int size = al.size();
                    int[] pos = new int[size];
                    int i = -1;
                    for (int x : al) {
                        pos[++i] = x;
                    }
                    type = getClass(cn, pos, 0, '.');
                    if (type == null) {
                        type = getClass(cn, pos, 0, '_');
                    }
                    if (type == null) {
                        type = getInnerClass(cn, pos, 0, '$');
                    }
                } catch (Exception ignored) {
                }
            } else {
                try {
                    type = Class.forName(className);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (type == null) {
                type = void.class;
            }
            ZibraClassManager.register(type, className);
        }
        return type;
    }


    private static Class<?> toClass(Type[] bounds) {
        if (bounds.length == 1) {
            Type boundType = bounds[0];
            if (boundType instanceof Class<?>) {
                return (Class<?>) boundType;
            }
        }
        return Object.class;
    }

    public static Class<?> toClass(Type type) {
        if (type == null) {
            return null;
        } else if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof WildcardType) {
            return toClass(((WildcardType) type).getUpperBounds());
        } else if (type instanceof TypeVariable) {
            return toClass(((TypeVariable) type).getBounds());
        } else if (type instanceof ParameterizedType) {
            return toClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(toClass(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else {
            return Object.class;
        }
    }

    public static Type getComponentType(Type type) {
        return (type instanceof GenericArrayType) ?
                ((GenericArrayType) type).getGenericComponentType() :
                (type instanceof ParameterizedType) ?
                        ((ParameterizedType) type).getActualTypeArguments()[0] :
                        ((Class<?>) type).isArray() ?
                                ((Class<?>) type).getComponentType() : Object.class;
    }

    public static Type getKeyType(Type type) {
        return (type instanceof ParameterizedType) ?
                ((ParameterizedType) type).getActualTypeArguments()[0] :
                Object.class;
    }

    public static Type getValueType(Type type) {
        return (type instanceof ParameterizedType) ?
                ((ParameterizedType) type).getActualTypeArguments()[1] :
                Object.class;
    }

    public static Type getActualType(Type type, Type paramType) {
        if ((type instanceof ParameterizedType) &&
                (paramType instanceof TypeVariable)) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            TypeVariable[] typeParameters = ((TypeVariable) paramType).getGenericDeclaration().getTypeParameters();
            int n = typeParameters.length;
            for (int i = 0; i < n; i++) {
                if (typeParameters[i].equals(paramType)) {
                    return actualTypeArguments[i];
                }
            }
        }
        return paramType;
    }
}
