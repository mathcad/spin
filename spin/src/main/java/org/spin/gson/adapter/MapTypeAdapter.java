package org.spin.gson.adapter;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class MapTypeAdapter implements JsonSerializer<LinkedTreeMap<String, Object>>, JsonDeserializer<LinkedTreeMap<String, Object>> {
    @Override
    public JsonElement serialize(LinkedTreeMap<String, Object> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject map = new JsonObject();
        for (Map.Entry entry : src.entrySet()) {
            Object value = entry.getValue();
            JsonElement valueElement;
            if (value == null) {
                valueElement = JsonNull.INSTANCE;
            } else {
                Type childType = value.getClass();
                valueElement = context.serialize(value, childType);
            }
            map.add(String.valueOf(entry.getKey()), valueElement);
        }
        return map;
    }

    @Override
    public LinkedTreeMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        LinkedTreeMap<String, Object> resultMap = new LinkedTreeMap<>();
        JsonObject jsonObject = json.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            if (entry.getValue() instanceof JsonPrimitive) {
                resultMap.put(entry.getKey(), entry.getValue().getAsString());
            } else {
                resultMap.put(entry.getKey(), entry.getValue());
            }
        }
        return resultMap;
    }
}
