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
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.matching.ContentPatternExtension;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class Json implements Serialiser {

  public static class PrivateView {}

  public static class PublicView {}

  public static Json get() {
    return holder.get();
  }

  private static final InheritableThreadLocal<Json> holder =
      new InheritableThreadLocal<>() {
        @Override
        protected Json initialValue() {
          return build(null);
        }
      };

  public static Json build(Extensions extensions) {
    final JsonMapper.Builder builder =
        JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
            .disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            .enable(
                JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                JsonParser.Feature.IGNORE_UNDEFINED,
                JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .serializationInclusion(JsonInclude.Include.NON_NULL);

    if (extensions != null) {
      final List<Class<?>> contentPatternExtensions =
          extensions.ofType(ContentPatternExtension.class).values().stream()
              .map(ContentPatternExtension::getContentPatternClass)
              .collect(toList());
      builder.registerSubtypes(contentPatternExtensions);
    }

    return new Json(builder.build());
  }

  private final JsonMapper mapper;

  private Json(JsonMapper mapper) {
    this.mapper = mapper;
  }

  public static <T> T read(byte[] bytes, Class<T> clazz) throws IOException {
    return holder.get().readValue(bytes, clazz);
  }

  @Override
  public <T> T readValue(byte[] bytes, Class<T> clazz) throws IOException {
    try {
      return mapper.readValue(bytes, clazz);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, Class<T> clazz) {
    return holder.get().readValue(json, clazz);
  }

  @Override
  public <T> T readValue(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> T read(String json, TypeReference<T> typeRef) {
    return holder.get().readValue(json, typeRef);
  }

  @Override
  public <T> T readValue(String json, TypeReference<T> typeRef) {
    try {
      return mapper.readValue(json, typeRef);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }

  public static <T> String write(T object) {
    return holder.get().writeString(object);
  }

  @Override
  public <T> String writeString(T object) {
    return writeString(object, PublicView.class);
  }

  public static <T> String writePrivate(T object) {
    return holder.get().writePrivateString(object);
  }

  @Override
  public <T> String writePrivateString(T object) {
    return writeString(object, PrivateView.class);
  }

  public static <T> String write(T object, Class<?> view) {
    return holder.get().writeString(object, view);
  }

  @Override
  public <T> String writeString(T object, Class<?> view) {
    try {
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
    return holder.get().mapper;
  }

  public static byte[] toByteArray(Object object) {
    return holder.get().writeBytes(object);
  }

  @Override
  public <T> byte[] writeBytes(T object) {
    try {
      return mapper.writeValueAsBytes(object);
    } catch (IOException ioe) {
      return throwUnchecked(ioe, byte[].class);
    }
  }

  public static JsonNode node(String json) {
    return holder.get().readNode(json);
  }

  @Override
  public JsonNode readNode(String json) {
    return readValue(json, JsonNode.class);
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
