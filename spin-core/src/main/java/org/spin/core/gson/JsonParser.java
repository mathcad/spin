/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spin.core.gson;

import org.spin.core.gson.internal.Streams;
import org.spin.core.gson.stream.JsonReader;
import org.spin.core.gson.stream.JsonToken;
import org.spin.core.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A parser to parse Json into a parse tree of {@link JsonElement}s
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public final class JsonParser {
    /**
     * @deprecated No need to instantiate this class, use the static methods instead.
     */
    @Deprecated
    public JsonParser() {
    }

    /**
     * Parses the specified JSON string into a parse tree
     *
     * @param json JSON text
     * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
     * @throws JsonParseException if the specified text is not valid JSON
     */
    public static JsonElement parseString(String json) throws JsonSyntaxException {
        return parseReader(new StringReader(json));
    }

    /**
     * Parses the specified JSON string into a parse tree
     *
     * @param reader JSON text
     * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
     * @throws JsonParseException if the specified text is not valid JSON
     */
    public static JsonElement parseReader(Reader reader) throws JsonIOException, JsonSyntaxException {
        try {
            JsonReader jsonReader = new JsonReader(reader);
            JsonElement element = parseReader(jsonReader);
            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            }
            return element;
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }
    }

    /**
     * Returns the next value from the JSON stream as a parse tree.
     *
     * @param reader reader
     * @return JsonElement
     * @throws JsonParseException if there is an IOException or if the specified
     *                            text is not valid JSON
     */
    public static JsonElement parseReader(JsonReader reader)
        throws JsonIOException, JsonSyntaxException {
        boolean lenient = reader.isLenient();
        reader.setLenient(true);
        try {
            return Streams.parse(reader);
        } catch (StackOverflowError | OutOfMemoryError e) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
        } finally {
            reader.setLenient(lenient);
        }
    }

    /**
     * @param json json
     * @return JsonElement
     * @deprecated Use {@link JsonParser#parseString}
     */
    @Deprecated
    public JsonElement parse(String json) throws JsonSyntaxException {
        return parseString(json);
    }

    /**
     * @param json json
     * @return JsonElement
     * @deprecated Use {@link JsonParser#parseReader(Reader)}
     */
    @Deprecated
    public JsonElement parse(Reader json) throws JsonIOException, JsonSyntaxException {
        return parseReader(json);
    }

    /**
     * @param json json
     * @return JsonElement
     * @deprecated Use {@link JsonParser#parseReader(JsonReader)}
     */
    @Deprecated
    public JsonElement parse(JsonReader json) throws JsonIOException, JsonSyntaxException {
        return parseReader(json);
    }
}
