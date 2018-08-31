package org.spin.core.util;

import org.spin.core.throwable.NullArgumentException;

import java.util.*;

/**
 * <p>
 * 操作Class的工具类（不使用反射）.
 * </p>
 * <p>
 * This class handles invalid <code>null</code> inputs as best it can. Each
 * method documents its behaviour in more detail.
 * </p>
 * <p>
 * The notion of a <code>canonical name</code> includes the human readable
 * name for the type, for example <code>int[]</code>. The non-canonical
 * method variants work with the JVM names, such as <code>[I</code>.
 * </p>
 *
 * @author Apache Software Foundation
 * @author Gary Gregory
 * @author Norm Deane
 * @author Alban Peignier
 * @author Tomasz Blachowicz
 * @version $DistributedId: ClassUtils.java 907121 2010-02-05 22:53:21Z mbenson $
 * @since 2.0
 */
public abstract class ClassUtils {
    /**
     * char形式的包名分隔符: <code>'&#x2e;' == {@value}</code>.
     */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /**
     * String形式的包名分隔符: <code>"&#x2e;"</code>.
     */
    public static final String PACKAGE_SEPARATOR = String.valueOf(PACKAGE_SEPARATOR_CHAR);

    /**
     * char形式的内部类名分隔符: <code>'$' == {@value}</code>.
     */
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';

    /**
     * String形式的内部类名分隔符: <code>"$"</code>.
     */
    public static final String INNER_CLASS_SEPARATOR = String.valueOf(INNER_CLASS_SEPARATOR_CHAR);

    /**
     * Maps primitive <code>Class</code>es to their corresponding wrapper
     * <code>Class</code>.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    /**
     * Maps wrapper <code>Class</code>es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();

    static {
        for (Iterator<Class<?>> it = primitiveWrapperMap.keySet().iterator(); it.hasNext(); ) {
            Class<?> primitiveClass = it.next();
            Class<?> wrapperClass = primitiveWrapperMap.get(primitiveClass);
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    /**
     * Maps a primitive class name to its corresponding abbreviation used in
     * array class names.
     */
    private static final Map<String, String> abbreviationMap = new HashMap<>();

    /**
     * Maps an abbreviation used in array class names to corresponding primitive
     * class name.
     */
    private static final Map<String, String> reverseAbbreviationMap = new HashMap<>();

    /**
     * Add primitive type abbreviation to maps of abbreviations.
     *
     * @param primitive    Canonical name of primitive type
     * @param abbreviation Corresponding abbreviation of primitive type
     */
    private static void addAbbreviation(String primitive, String abbreviation) {
        abbreviationMap.put(primitive, abbreviation);
        reverseAbbreviationMap.put(abbreviation, primitive);
    }

    /*
     * Feed abbreviation maps
     */
    static {
        addAbbreviation("int", "I");
        addAbbreviation("boolean", "Z");
        addAbbreviation("float", "F");
        addAbbreviation("long", "J");
        addAbbreviation("short", "S");
        addAbbreviation("byte", "B");
        addAbbreviation("double", "D");
        addAbbreviation("char", "C");
    }

    /**
     * <p>
     * ClassUtils instances should NOT be constructed in standard programming.
     * Instead, the class should be used as
     * <code>ClassUtils.getShortClassName(cls)</code>.
     * </p>
     * <p>
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     * </p>
     */
    private ClassUtils() {
    }

    // Short class name
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Gets the class name minus the package name for an <code>Object</code>.
     * </p>
     *
     * @param object      the class to get the short name for, may be null
     * @param valueIfNull the value to return if null
     * @return the class name of the object without the package name, or the
     * null value
     */
    public static String getShortClassName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return getShortClassName(object.getClass());
    }

    /**
     * <p>
     * Gets the class name minus the package name from a <code>Class</code>.
     * </p>
     *
     * @param cls the class to get the short name for.
     * @return the class name without the package name or an empty string
     */
    public static String getShortClassName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getShortClassName(cls.getName());
    }

    /**
     * <p>
     * Gets the class name minus the package name from a String.
     * </p>
     * <p>
     * The string passed in is assumed to be a class name - it is not checked.
     * </p>
     *
     * @param className the className to get the short name for
     * @return the class name of the class without the package name or an empty
     * string
     */
    public static String getShortClassName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
            return StringUtils.EMPTY;
        }

        StringBuilder arrayPrefix = new StringBuilder();

        // Handle array encoding
        if (className.startsWith("[")) {
            while (className.charAt(0) == '[') {
                className = className.substring(1);
                arrayPrefix.append("[]");
            }
            // Strip Object type encoding
            if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
                className = className.substring(1, className.length() - 1);
            }
        }

        if (reverseAbbreviationMap.containsKey(className)) {
            className = reverseAbbreviationMap.get(className);
        }

        int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        int innerIdx = className.indexOf(INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
        String out = className.substring(lastDotIdx + 1);
        if (innerIdx != -1) {
            out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        }
        return out + arrayPrefix;
    }

    // Package name
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Gets the package name of an <code>Object</code>.
     * </p>
     *
     * @param object      the class to get the package name for, may be null
     * @param valueIfNull the value to return if null
     * @return the package name of the object, or the null value
     */
    public static String getPackageName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return getPackageName(object.getClass());
    }

    /**
     * <p>
     * Gets the package name of a <code>Class</code>.
     * </p>
     *
     * @param cls the class to get the package name for, may be
     *            <code>null</code>.
     * @return the package name or an empty string
     */
    public static String getPackageName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getPackageName(cls.getName());
    }

    /**
     * <p>
     * Gets the package name from a <code>String</code>.
     * </p>
     * <p>
     * The string passed in is assumed to be a class name - it is not checked.
     * </p>
     * <p>
     * If the class is unpackaged, return an empty string.
     * </p>
     *
     * @param className the className to get the package name for, may be
     *                  <code>null</code>
     * @return the package name or an empty string
     */
    public static String getPackageName(String className) {
        if (className == null || className.length() == 0) {
            return StringUtils.EMPTY;
        }

        // Strip array encoding
        while (className.charAt(0) == '[') {
            className = className.substring(1);
        }
        // Strip Object type encoding
        if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1);
        }

        int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (i == -1) {
            return StringUtils.EMPTY;
        }
        return className.substring(0, i);
    }

    // Superclasses/Superinterfaces
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Gets a <code>List</code> of superclasses for the given class.
     * </p>
     *
     * @param cls the class to look up, may be <code>null</code>
     * @return the <code>List</code> of superclasses in order going up from this
     * one <code>empty list</code> if null input
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> cls) {
        List<Class<?>> classes = new ArrayList<>();
        if (cls == null) {
            return classes;
        }
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    /**
     * <p>
     * Gets a <code>List</code> of all interfaces implemented by the given class
     * and its superclasses.
     * </p>
     * <p>
     * The order is determined by looking through each interface in turn as
     * declared in the source file and following its hierarchy up. Then each
     * superclass is considered in the same way. Later duplicates are ignored,
     * so the order is maintained.
     * </p>
     *
     * @param cls the class to look up, may be <code>null</code>
     * @return the <code>List</code> of interfaces in order, <code>empty list</code>
     * if null input
     */
    public static List<Class<?>> getAllInterfaces(Class<?> cls) {
        List<Class<?>> interfacesFound = new ArrayList<>();
        if (cls == null) {
            return interfacesFound;
        }

        getAllInterfaces(cls, interfacesFound);

        return interfacesFound;
    }

    /**
     * Get the interfaces for the specified class.
     *
     * @param cls             the class to look up, may be <code>null</code>
     * @param interfacesFound the <code>Set</code> of interfaces for the class
     */
    private static void getAllInterfaces(Class<?> cls, List<Class<?>> interfacesFound) {
        while (cls != null) {
            Class<?>[] interfaces = cls.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                if (!interfacesFound.contains(interfaces[i])) {
                    interfacesFound.add(interfaces[i]);
                    getAllInterfaces(interfaces[i], interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    // Convert list
    // ----------------------------------------------------------------------

    /**
     * <p>Given a <code>List</code> of class names, this method converts them into classes.</p>
     * <p>A new <code>List</code> is returned. If the class name cannot be found, <code>null</code> is stored in the <code>List</code>. If the class name
     * in the <code>List</code> is <code>null</code>, <code>null</code> is stored in the output <code>List</code>.</p>
     *
     * @param classNames the classNames to change
     * @return a <code>List</code> of Class objects corresponding to the class names, <code>empty list</code> if null input
     * @throws ClassCastException if classNames contains a non String entry
     */
    public static List<Class<?>> convertClassNamesToClasses(List<?> classNames) {
        if (classNames == null) {
            return new ArrayList<>();
        }
        List<Class<?>> classes = new ArrayList<>(classNames.size());
        for (Iterator<?> it = classNames.iterator(); it.hasNext(); ) {
            String className = (String) it.next();
            try {
                classes.add(Class.forName(className));
            } catch (Exception ex) {
                classes.add(null);
            }
        }
        return classes;
    }

    /**
     * <p>
     * Given a <code>List</code> of <code>Class</code> objects, this method
     * converts them into class names.
     * </p>
     * <p>
     * A new <code>List</code> is returned. <code>null</code> objects will be
     * copied into the returned list as <code>null</code>.
     * </p>
     *
     * @param classes the classes to change
     * @return a <code>List</code> of class names corresponding to the Class
     * objects, <code>empty list</code> if null input
     * @throws ClassCastException if <code>classes</code> contains a non-<code>Class</code>
     *                            entry
     */
    public static List<String> convertClassesToClassNames(List<Class<?>> classes) {
        if (classes == null) {
            return new ArrayList<>();
        }
        List<String> classNames = new ArrayList<>(classes.size());
        for (Iterator<Class<?>> it = classes.iterator(); it.hasNext(); ) {
            Class<?> cls = it.next();
            if (cls == null) {
                classNames.add(null);
            } else {
                classNames.add(cls.getName());
            }
        }
        return classNames;
    }

    /**
     * <p>
     * Checks if one <code>Class</code> can be assigned to a variable of another
     * <code>Class</code>.
     * </p>
     * <p>
     * Unlike the {@link Class#isAssignableFrom(Class)} method, this
     * method takes into account widenings of primitive classes and
     * <code>null</code>s.
     * </p>
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or
     * double. This method returns the correct result for these cases.
     * </p>
     * <p>
     * <code>Null</code> may be assigned to any reference type. This method will
     * return <code>true</code> if <code>null</code> is passed in and the
     * toClass is non-primitive.
     * </p>
     * <p>
     * Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * widening primitive or widening reference conversion. See
     * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
     * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     *
     * @param cls     the Class to check, may be null
     * @param toClass the Class to try to assign into, returns false if null
     * @return <code>true</code> if assignment possible
     */
    public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
        return isAssignable(cls, toClass, false);
    }

    /**
     * <p>
     * Checks if one <code>Class</code> can be assigned to a variable of another
     * <code>Class</code>.
     * </p>
     * <p>
     * Unlike the {@link Class#isAssignableFrom(Class)} method, this
     * method takes into account widenings of primitive classes and
     * <code>null</code>s.
     * </p>
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or
     * double. This method returns the correct result for these cases.
     * </p>
     * <p>
     * <code>Null</code> may be assigned to any reference type. This method will
     * return <code>true</code> if <code>null</code> is passed in and the
     * toClass is non-primitive.
     * </p>
     * <p>
     * Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * widening primitive or widening reference conversion. See
     * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
     * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     *
     * @param cls        the Class to check, may be null
     * @param toClass    the Class to try to assign into, returns false if null
     * @param autoboxing whether to use implicit autoboxing/unboxing between primitives
     *                   and wrappers
     * @return <code>true</code> if assignment possible
     * @since 2.5
     */
    public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !(toClass.isPrimitive());
        }
        // autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                    || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                    || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass)
                    || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    /**
     * <p>
     * Converts the specified primitive Class object to its corresponding
     * wrapper Class object.
     * </p>
     * <p>
     * NOTE: From v2.2, this method handles <code>Void.TYPE</code>, returning
     * <code>Void.TYPE</code>.
     * </p>
     *
     * @param cls the class to convert, may be null
     * @return the wrapper class for <code>cls</code> or <code>cls</code> if
     * <code>cls</code> is not a primitive. <code>null</code> if null
     * input.
     * @since 2.1
     */
    public static Class<?> primitiveToWrapper(Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    /**
     * <p>
     * Converts the specified array of primitive Class objects to an array of
     * its corresponding wrapper Class objects.
     * </p>
     *
     * @param classes the class array to convert, may be null or empty
     * @return an array which contains for each given class, the wrapper class
     * or the original class if class is not a primitive.
     * <code>null</code> if null input. Empty array if an empty array
     * passed in.
     * @since 2.1
     */
    public static Class<?>[] primitivesToWrappers(Class<?>[] classes) {
        if (classes == null) {
            return null;
        }

        if (classes.length == 0) {
            return classes;
        }

        Class<?>[] convertedClasses = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            convertedClasses[i] = primitiveToWrapper(classes[i]);
        }
        return convertedClasses;
    }

    /**
     * <p>
     * Converts the specified wrapper class to its corresponding primitive
     * class.
     * </p>
     * <p>
     * This method is the counter part of <code>primitiveToWrapper()</code>. If
     * the passed in class is a wrapper class for a primitive type, this
     * primitive type will be returned (e.g. <code>Integer.TYPE</code> for
     * <code>Integer.class</code>). For other classes, or if the parameter is
     * <b>null</b>, the return value is <b>null</b>.
     * </p>
     *
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if <code>cls</code> is a wrapper
     * class, <b>null</b> otherwise
     * @see #primitiveToWrapper(Class)
     * @since 2.4
     */
    public static Class<?> wrapperToPrimitive(Class<?> cls) {
        if (null == cls || cls.isPrimitive())
            return cls;
        return wrapperPrimitiveMap.get(cls);
    }

    /**
     * <p>
     * Converts the specified array of wrapper Class objects to an array of its
     * corresponding primitive Class objects.
     * </p>
     * <p>
     * This method invokes <code>wrapperToPrimitive()</code> for each element of
     * the passed in array.
     * </p>
     *
     * @param classes the class array to convert, may be null or empty
     * @return an array which contains for each given class, the primitive class
     * or <b>null</b> if the original class is not a wrapper class.
     * <code>null</code> if null input. Empty array if an empty array
     * passed in.
     * @see #wrapperToPrimitive(Class)
     * @since 2.4
     */
    public static Class<?>[] wrappersToPrimitives(Class<?>[] classes) {
        if (classes == null) {
            return null;
        }

        if (classes.length == 0) {
            return classes;
        }

        Class<?>[] convertedClasses = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            convertedClasses[i] = wrapperToPrimitive(classes[i]);
        }
        return convertedClasses;
    }

    // Inner class
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Is the specified class an inner class or static nested class.
     * </p>
     *
     * @param cls the class to check, may be null
     * @return <code>true</code> if the class is an inner or static nested
     * class, false if not or <code>null</code>
     */
    public static boolean isInnerClass(Class<?> cls) {
        return cls != null && cls.getName().indexOf(INNER_CLASS_SEPARATOR_CHAR) >= 0;
    }

    // Class loading
    // ----------------------------------------------------------------------

    /**
     * Returns the class represented by <code>className</code> using the
     * <code>classLoader</code>. This implementation supports names like
     * "<code>java.lang.String[]</code>" as well as
     * "<code>[Ljava.lang.String;</code>".
     *
     * @param classLoader the class loader to use to load the class
     * @param className   the class name
     * @param initialize  whether the class must be initialized
     * @return the class represented by <code>className</code> using the
     * <code>classLoader</code>
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class<?> getClass(ClassLoader classLoader, String className, boolean initialize)
        throws ClassNotFoundException {
        Class<?> clazz;
        if (abbreviationMap.containsKey(className)) {
            String clsName = "[" + abbreviationMap.get(className);
            clazz = Class.forName(clsName, initialize, classLoader).getComponentType();
        } else {
            clazz = Class.forName(toCanonicalName(className), initialize, classLoader);
        }
        return clazz;
    }

    /**
     * Returns the (initialized) class represented by <code>className</code>
     * using the <code>classLoader</code>. This implementation supports names
     * like "<code>java.lang.String[]</code>" as well as
     * "<code>[Ljava.lang.String;</code>".
     *
     * @param classLoader the class loader to use to load the class
     * @param className   the class name
     * @return the class represented by <code>className</code> using the
     * <code>classLoader</code>
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class<?> getClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        return getClass(classLoader, className, true);
    }

    /**
     * Returns the (initialized) class represented by <code>className</code>
     * using the current thread's context class loader. This implementation
     * supports names like "<code>java.lang.String[]</code>" as well as
     * "<code>[Ljava.lang.String;</code>".
     *
     * @param className the class name
     * @return the class represented by <code>className</code> using the current
     * thread's context class loader
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return getClass(className, true);
    }

    /**
     * Returns the class represented by <code>className</code> using the current
     * thread's context class loader. This implementation supports names like
     * "<code>java.lang.String[]</code>" as well as
     * "<code>[Ljava.lang.String;</code>".
     *
     * @param className  the class name
     * @param initialize whether the class must be initialized
     * @return the class represented by <code>className</code> using the current
     * thread's context class loader
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class<?> getClass(String className, boolean initialize) throws ClassNotFoundException {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = contextCL == null ? ClassUtils.class.getClassLoader() : contextCL;
        return getClass(loader, className, initialize);
    }

    // ----------------------------------------------------------------------

    /**
     * Converts a class name to a JLS style class name.
     *
     * @param className the class name
     * @return the converted name
     */
    private static String toCanonicalName(String className) {
        className = StringUtils.deleteWhitespace(className);
        if (className == null) {
            throw new NullArgumentException("className");
        } else if (className.endsWith("[]")) {
            StringBuilder classNameBuffer = new StringBuilder();
            while (className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
                classNameBuffer.append("[");
            }
            String abbreviation = abbreviationMap.get(className);
            if (abbreviation != null) {
                classNameBuffer.append(abbreviation);
            } else {
                classNameBuffer.append("L").append(className).append(";");
            }
            className = classNameBuffer.toString();
        }
        return className;
    }

    // Short canonical name
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Gets the canonical name minus the package name for an <code>Object</code>
     * .
     * </p>
     *
     * @param object      the class to get the short name for, may be null
     * @param valueIfNull the value to return if null
     * @return the canonical name of the object without the package name, or the
     * null value
     * @since 2.4
     */
    public static String getShortCanonicalName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return getShortCanonicalName(object.getClass().getName());
    }

    /**
     * <p>
     * Gets the canonical name minus the package name from a <code>Class</code>.
     * </p>
     *
     * @param cls the class to get the short name for.
     * @return the canonical name without the package name or an empty string
     * @since 2.4
     */
    public static String getShortCanonicalName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getShortCanonicalName(cls.getName());
    }

    /**
     * <p>
     * Gets the canonical name minus the package name from a String.
     * </p>
     * <p>
     * The string passed in is assumed to be a canonical name - it is not
     * checked.
     * </p>
     *
     * @param canonicalName the class name to get the short name for
     * @return the canonical name of the class without the package name or an
     * empty string
     * @since 2.4
     */
    public static String getShortCanonicalName(String canonicalName) {
        return ClassUtils.getShortClassName(getCanonicalName(canonicalName));
    }

    // Package name
    // ----------------------------------------------------------------------

    /**
     * <p>
     * Gets the package name from the canonical name of an <code>Object</code>.
     * </p>
     *
     * @param object      the class to get the package name for, may be null
     * @param valueIfNull the value to return if null
     * @return the package name of the object, or the null value
     * @since 2.4
     */
    public static String getPackageCanonicalName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return getPackageCanonicalName(object.getClass().getName());
    }

    /**
     * <p>
     * Gets the package name from the canonical name of a <code>Class</code>.
     * </p>
     *
     * @param cls the class to get the package name for, may be
     *            <code>null</code>.
     * @return the package name or an empty string
     * @since 2.4
     */
    public static String getPackageCanonicalName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getPackageCanonicalName(cls.getName());
    }

    /**
     * <p>
     * Gets the package name from the canonical name.
     * </p>
     * <p>
     * The string passed in is assumed to be a canonical name - it is not
     * checked.
     * </p>
     * <p>
     * If the class is unpackaged, return an empty string.
     * </p>
     *
     * @param canonicalName the canonical name to get the package name for, may be
     *                      <code>null</code>
     * @return the package name or an empty string
     * @since 2.4
     */
    public static String getPackageCanonicalName(String canonicalName) {
        return ClassUtils.getPackageName(getCanonicalName(canonicalName));
    }

    /**
     * 判断两个类是否相同（类名完全相同，类加载器相同）
     *
     * @param cls1 参数1
     * @param cls2 参数2
     * @return 是否相同
     */
    public static boolean equal(Class<?> cls1, Class<?> cls2) {
        return cls1 == cls2 || null != cls1 && null != cls2 && cls1.getName().equals(cls2.getName()) && cls1.getClassLoader() == cls2.getClassLoader();
    }

    /**
     * <p>
     * Converts a given name of class into canonical format. If name of class is
     * not a name of array class it returns unchanged name.
     * </p>
     * <p>
     * Example:
     * <ul>
     * <li><code>getCanonicalName("[I") = "int[]"</code></li>
     * <li>
     * <code>getCanonicalName("[Ljava.lang.String;") = "java.lang.String[]"</code>
     * </li>
     * <li>
     * <code>getCanonicalName("java.lang.String") = "java.lang.String"</code>
     * </li>
     * </ul>
     * </p>
     *
     * @param className the name of class
     * @return canonical form of class name
     * @since 2.4
     */
    private static String getCanonicalName(String className) {
        className = StringUtils.deleteWhitespace(className);
        if (className == null) {
            return null;
        } else {
            int dim = 0;
            while (className.startsWith("[")) {
                dim++;
                className = className.substring(1);
            }
            if (dim < 1) {
                return className;
            } else {
                if (className.startsWith("L")) {
                    className = className.substring(1,
                        className.endsWith(";") ? className.length() - 1 : className.length());
                } else {
                    if (className.length() > 0) {
                        className = reverseAbbreviationMap.get(className.substring(0, 1));
                    }
                }
                StringBuilder canonicalClassNameBuffer = new StringBuilder(className);
                for (int i = 0; i < dim; i++) {
                    canonicalClassNameBuffer.append("[]");
                }
                return canonicalClassNameBuffer.toString();
            }
        }
    }
}
