package org.spin.core.gson.internal.bind;

import org.spin.core.gson.FieldNamingStrategy;
import org.spin.core.gson.Gson;
import org.spin.core.gson.JsonSyntaxException;
import org.spin.core.gson.MatchableTypeAdapter;
import org.spin.core.gson.TypeAdapter;
import org.spin.core.gson.TypeAdapterFactory;
import org.spin.core.gson.annotation.JsonAdapter;
import org.spin.core.gson.annotation.SerializedName;
import org.spin.core.gson.internal.$Gson$Types;
import org.spin.core.gson.internal.ConstructorConstructor;
import org.spin.core.gson.internal.Excluder;
import org.spin.core.gson.internal.ObjectConstructor;
import org.spin.core.gson.internal.Primitives;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.JsonWriter;
import org.spin.core.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Type adapter that reflects over the fields and methods of a class.
 * <p>Created by xuweinan on 2018/3/1.</p>
 *
 * @author xuweinan
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final Excluder excluder;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
                                        FieldNamingStrategy fieldNamingPolicy, Excluder excluder,
                                        JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.jsonAdapterFactory = jsonAdapterFactory;
    }

    public boolean excludeField(Field f, boolean serialize) {
        return excludeField(f, serialize, excluder);
    }

    static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
        return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
    }

    /**
     * first element holds the default name
     */
    private List<String> getFieldNames(Field f) {
        SerializedName annotation = f.getAnnotation(SerializedName.class);
        if (annotation == null) {
            String name = fieldNamingPolicy.translateName(f);
            if (name.hashCode() == f.getName().hashCode()) {
                return Collections.singletonList(name);
            } else {
                return CollectionUtils.ofArrayList(name, f.getName());
            }
        }

        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }

        List<String> fieldNames = new ArrayList<>(alternates.length + 1);
        fieldNames.add(serializedName);
        fieldNames.addAll(Arrays.asList(alternates));
        return fieldNames;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();

        if (!Object.class.isAssignableFrom(raw)) {
            return null; // it's a primitive!
        }

        ObjectConstructor<T> constructor = constructorConstructor.get(type);
        return new Adapter<>(constructor, getBoundFields(gson, type, raw));
    }

    private BoundField createBoundField(
        final Gson context, final Field field, final String name,
        final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        // special casing primitives here saves ~5% on Android...
        JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = jsonAdapterFactory.getTypeAdapter(
                constructorConstructor, context, fieldType, annotation);
        }
        final boolean jsonAdapterPresent = mapped != null;
        if (mapped == null) mapped = context.getAdapter(fieldType);

        final TypeAdapter<?> typeAdapter = mapped;
        return new BoundField(name, serialize, deserialize) {
            @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
            @Override
            void write(JsonWriter writer, Object value)
                throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                if (typeAdapter instanceof MatchableTypeAdapter) {
                    ((MatchableTypeAdapter) typeAdapter).write(writer, fieldValue, field);
                } else {
                    TypeAdapter t = jsonAdapterPresent ? typeAdapter
                        : new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
                    if (t instanceof TypeAdapterRuntimeTypeWrapper) {
                        ((TypeAdapterRuntimeTypeWrapper) t).write(writer, fieldValue, field);
                    } else {
                        t.write(writer, fieldValue);
                    }
                }
            }

            @Override
            void read(JsonReader reader, Object value)
                throws IOException, IllegalAccessException {
                Object fieldValue = typeAdapter instanceof MatchableTypeAdapter ? ((MatchableTypeAdapter<?>) typeAdapter).read(reader, fieldType, field) : typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }

            @Override
            public boolean writeField(Object value) throws IllegalAccessException {
                if (!serialized) return false;
                Object fieldValue = field.get(value);
                return fieldValue != value; // avoid recursion for example for Throwable.cause
            }
        };
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (raw.isInterface()) {
            return result;
        }

        Type declaredType = type.getType();
        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                boolean serialize = excludeField(field, true);
                boolean deserialize = excludeField(field, false);
                if (!serialize && !deserialize) {
                    continue;
                }
                field.setAccessible(true);
                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
                List<String> fieldNames = getFieldNames(field);
                BoundField previous = null;
                for (int i = 0, size = fieldNames.size(); i < size; ++i) {
                    String name = fieldNames.get(i);
                    if (i != 0) serialize = false; // only serialize the default name
                    BoundField boundField = createBoundField(context, field, name,
                        TypeToken.get(fieldType), serialize, deserialize);
                    BoundField replaced = result.put(name, boundField);
                    if (previous == null) previous = replaced;
                }
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType
                        + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    abstract static class BoundField {
        final String name;
        final boolean serialized;
        final boolean deserialized;

        protected BoundField(String name, boolean serialized, boolean deserialized) {
            this.name = name;
            this.serialized = serialized;
            this.deserialized = deserialized;
        }

        abstract boolean writeField(Object value) throws IOException, IllegalAccessException;

        abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;

        abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
    }

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final ObjectConstructor<T> constructor;
        private final Map<String, BoundField> boundFields;

        Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            T instance = constructor.construct();

            try {
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = boundFields.get(name);
                    if (field == null || !field.deserialized) {
                        in.skipValue();
                    } else {
                        field.read(in, instance);
                    }
                }
            } catch (IllegalStateException e) {
                throw new JsonSyntaxException(e);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            in.endObject();
            return instance;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            try {
                for (BoundField boundField : boundFields.values()) {
                    if (boundField.writeField(value)) {
                        out.name(boundField.name);
                        boundField.write(out, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            out.endObject();
        }
    }
}
