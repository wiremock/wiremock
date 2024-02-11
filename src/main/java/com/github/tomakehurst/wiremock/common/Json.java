/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Json {

  public static class PrivateView {}

  public static class PublicView {}

  private static final InheritableThreadLocal<ObjectMapper> objectMapperHolder =
      new InheritableThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.setNodeFactory(new JsonNodeFactory(true));
          objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
          objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
          objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
          objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
          objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
          objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
          objectMapper.registerModule(new JavaTimeModule());
          objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          objectMapper.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
          return objectMapper;
        }
      };

  private Json() {}

  public static <T> T read(byte[] stream, Class<T> clazz) throws IOException {
    try {
      ObjectMapper mapper = getObjectMapper();
      return mapper.readValue(stream, clazz);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, Class<T> clazz) {
    try {
      ObjectMapper mapper = getObjectMapper();
      return mapper.readValue(json, clazz);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, TypeReference<T> typeRef) {
    try {
      ObjectMapper mapper = getObjectMapper();
      return mapper.readValue(json, typeRef);
    } catch (JsonProcessingException processingException) {
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
    try {
      ObjectMapper mapper = getObjectMapper();
      ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
      if (view != null) {
        objectWriter = objectWriter.withView(view);
      }
      return objectWriter.writeValueAsString(object);
    } catch (IOException ioe) {
      return throwUnchecked(ioe, String.class);
    }
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapperHolder.get();
  }

  public static byte[] toByteArray(Object object) {
    try {
      ObjectMapper mapper = getObjectMapper();
      return mapper.writeValueAsBytes(object);
    } catch (IOException ioe) {
      return throwUnchecked(ioe, byte[].class);
    }
  }

  public static byte[] toByteArrayEscaped(JsonNode jsonNode) {
    String string = toStringEscaped(jsonNode);
    return string != null ? Strings.bytesFromString(string) : new byte[0];
  }

  public static String toStringEscaped(JsonNode jsonNode) {
    if (jsonNode.isValueNode()) {
      if (jsonNode.isTextual()) {
        return "\"" + jsonNode.asText() + "\"";
      } else {
        return jsonNode.asText();
      }
    } else if (jsonNode.isArray()) {
      return Stream.generate(jsonNode.elements()::next)
          .limit(jsonNode.size())
          .map(Json::toStringEscaped)
          .collect(Collectors.joining(",", "[", "]"));
    } else if (jsonNode.isObject()) {
      return Stream.generate(jsonNode.fields()::next)
          .limit(jsonNode.size())
          .map(field -> "\"" + field.getKey() + "\":" + toStringEscaped(field.getValue()))
          .collect(Collectors.joining(",", "{", "}"));
    }
    return null;
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
    if (node.isContainerNode()) {
      for (JsonNode child : node) {
        acc++;
        if (child.isContainerNode()) {
          acc += deepSize(child);
        }
      }
    }

    return acc;
  }

  public static String prettyPrint(String json) {
    ObjectMapper mapper = getObjectMapper();
    try {
      return mapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(mapper.readValue(json, JsonNode.class));
    } catch (IOException e) {
      return throwUnchecked(e, String.class);
    }
  }

  public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass) {
    ObjectMapper mapper = getObjectMapper();
    return mapper.convertValue(map, targetClass);
  }

  public static <T> Map<String, Object> objectToMap(T theObject) {
    ObjectMapper mapper = getObjectMapper();
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
