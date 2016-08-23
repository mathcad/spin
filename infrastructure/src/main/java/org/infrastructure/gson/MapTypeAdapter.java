package org.infrastructure.gson;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;

public class MapTypeAdapter implements JsonSerializer<LinkedTreeMap>, JsonDeserializer<LinkedTreeMap> {

	@Override
	public LinkedTreeMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		LinkedTreeMap resultMap = new LinkedTreeMap();
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

	@Override
	public JsonElement serialize(LinkedTreeMap src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject map = new JsonObject();
		for (Map.Entry entry : (Set<Map.Entry>) src.entrySet()) {
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

}
