package org.spin.data.rs;

import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.util.JsonUtils;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RowMappers {
    private static final ConcurrentHashMap<TypeToken<?>, RowMapper<?>> MAPPERS = new ConcurrentHashMap<>();

    static {
        MAPPERS.put(JsonUtils.MAP_TYPE_TOKEN, new MapRowMapper());
    }

    public static <T> void registerMapper(TypeToken<T> typeToken, RowMapper<T> mapper) {
        MAPPERS.put(typeToken, mapper);
    }

    public static MapRowMapper getMapRowMapper() {
        return (MapRowMapper) MAPPERS.get(JsonUtils.MAP_TYPE_TOKEN);
    }

    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> getMapper(TypeToken<T> typeToken) {
        if (MAPPERS.containsKey(typeToken)) {
            return (RowMapper<T>) MAPPERS.get(typeToken);
        } else {
            Type actType = typeToken.getType();
            if (actType instanceof Class) {
                if (Object.class == actType || Map.class == actType) {
                    return (RowMapper<T>) getMapRowMapper();
                }
                EntityRowMapper<?> erm = new EntityRowMapper<>((Class<?>) actType);
                MAPPERS.put(typeToken, erm);
                return (RowMapper<T>) erm;
            } else {
                throw new SQLException(SQLError.UNKNOW_MAPPER_SQL_TYPE, "未知映射类型: " + typeToken);
            }
        }
    }

    public static <T> RowMapper<T> getMapper(Class<T> actType) {
        TypeToken<T> tTypeToken = TypeToken.get(actType);
        return getMapper(tTypeToken);
    }
}
