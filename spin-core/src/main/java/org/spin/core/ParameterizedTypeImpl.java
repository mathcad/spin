package org.spin.core;

import org.spin.core.gson.internal.$Gson$Preconditions;
import org.spin.core.gson.internal.$Gson$Types;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private final Type ownerType;

    private ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {

        this.actualTypeArguments = actualTypeArguments.clone();
        for (int t = 0, length = this.actualTypeArguments.length; t < length; t++) {
            $Gson$Preconditions.checkNotNull(this.actualTypeArguments[t]);
            if ((this.actualTypeArguments[t] instanceof Class<?>) && ((Class<?>) this.actualTypeArguments[t]).isPrimitive()) {
                throw new IllegalArgumentException("泛型参数不允许存在基本类型");
            }
            this.actualTypeArguments[t] = $Gson$Types.canonicalize(this.actualTypeArguments[t]);
        }
        this.rawType = rawType;
        this.ownerType = ownerType != null ? ownerType : rawType.getDeclaringClass();
        this.validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable[] typeVariable = this.rawType.getTypeParameters();
        if (typeVariable.length != this.actualTypeArguments.length) {
            throw new MalformedParameterizedTypeException();
        } else {
            for (int idx = 0; idx < this.actualTypeArguments.length; ++idx) {
                // do nothing
            }

        }
    }

    public static ParameterizedTypeImpl make(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
    }

    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments.clone();
    }

    public Class<?> getRawType() {
        return this.rawType;
    }

    public Type getOwnerType() {
        return this.ownerType;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) obj;
            if (this == parameterizedType) {
                return true;
            } else {
                Type ownerType = parameterizedType.getOwnerType();
                Type rawType = parameterizedType.getRawType();
                return Objects.equals(this.ownerType, ownerType) && Objects.equals(this.rawType, rawType) && Arrays.equals(this.actualTypeArguments, parameterizedType.getActualTypeArguments());
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.ownerType != null) {
            if (this.ownerType instanceof Class) {
                sb.append(((Class) this.ownerType).getName());
            } else {
                sb.append(this.ownerType.toString());
            }

            sb.append("$");
            if (this.ownerType instanceof ParameterizedTypeImpl) {
                sb.append(this.rawType.getName().replace(((ParameterizedTypeImpl) this.ownerType).rawType.getName() + "$", ""));
            } else {
                sb.append(this.rawType.getSimpleName());
            }
        } else {
            sb.append(this.rawType.getName());
        }

        if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
            sb.append("<");
            boolean b = true;
            Type[] actualTypeArguments = this.actualTypeArguments;
            int length = actualTypeArguments.length;

            for (int i = 0; i < length; ++i) {
                Type actualTypeArgument = actualTypeArguments[i];
                if (!b) {
                    sb.append(", ");
                }

                sb.append(actualTypeArgument.getTypeName());
                b = false;
            }

            sb.append(">");
        }

        return sb.toString();
    }
}
