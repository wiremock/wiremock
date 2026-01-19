/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.IOException;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public final class Json {

  public static class PrivateView {}

  public static class PublicView {}

  private static final InheritableThreadLocal<JsonMapper> jsonMapperHolder =
      new InheritableThreadLocal<>() {
        @Override
        protected JsonMapper initialValue() {
          return JsonMapper.builder()
              .configureForJackson2()
              .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
              .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(Include.NON_NULL))
              // enable
              .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
              .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
              .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
              .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
              .enable(StreamReadFeature.IGNORE_UNDEFINED)
              .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
              .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
              // disable
              .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
              .build();
        }
      };

  private Json() {}

  public static <T> T read(byte[] stream, Class<T> clazz) throws IOException {
    try {
      JsonMapper mapper = getJsonMapper();
      return mapper.readValue(stream, clazz);
    } catch (JacksonException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, Class<T> clazz) {
    try {
      JsonMapper mapper = getJsonMapper();
      return mapper.readValue(json, clazz);
    } catch (JacksonException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, TypeReference<T> typeRef) {
    try {
      JsonMapper mapper = getJsonMapper();
      return mapper.readValue(json, typeRef);
    } catch (JacksonException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> String write(T object) {
    return write(object, PublicView.class);
  }

  public static <T> String writePrivate(T object) {
    return write(object, PrivateView.class);
  }

  public static <T> String write(T object, Class<?> view) {
    JsonMapper mapper = getJsonMapper();
    ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
    if (view != null) {
      objectWriter = objectWriter.withView(view);
    }
    return objectWriter.writeValueAsString(object);
  }

  public static JsonMapper getJsonMapper() {
    return jsonMapperHolder.get();
  }

  public static byte[] toByteArray(Object object) {
    JsonMapper mapper = getJsonMapper();
    return mapper.writeValueAsBytes(object);
  }

  public static JsonNode node(String json) {
    return read(json, JsonNode.class);
  }

  public static int maxDeepSize(JsonNode one, JsonNode two) {
    return Math.max(deepSize(one), deepSize(two));
  }

  public static int deepSize(JsonNode node) {
    if (node == null) {
      return 0;
    }

    int acc = 1;
    if (node.isContainer()) {
      for (JsonNode child : node) {
        acc++;
        if (child.isContainer()) {
          acc += deepSize(child);
        }
      }
    }

    return acc;
  }

  public static String prettyPrint(String json) {
    JsonMapper mapper = getJsonMapper();
    return mapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(mapper.readValue(json, JsonNode.class));
  }

  public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass) {
    JsonMapper mapper = getJsonMapper();
    return mapper.convertValue(map, targetClass);
  }

  public static <T> Map<String, Object> objectToMap(T theObject) {
    JsonMapper mapper = getJsonMapper();
    return mapper.convertValue(theObject, new TypeReference<Map<String, Object>>() {});
  }

  public static int schemaPropertyCount(JsonNode schema) {
    int count = 0;
    final JsonNode propertiesNode = schema.get("properties");
    if (propertiesNode != null && !propertiesNode.isEmpty()) {
      for (JsonNode property : propertiesNode) {
        count++;
        if (property.has("properties")) {
          count += schemaPropertyCount(property);
        }
      }
    }

    return count;
  }
}
