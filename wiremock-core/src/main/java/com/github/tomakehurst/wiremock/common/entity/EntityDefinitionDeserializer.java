/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.entity;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.entity.EntityDefinition.DEFAULT_CHARSET;
import static com.github.tomakehurst.wiremock.common.entity.Format.detectFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.tomakehurst.wiremock.common.Encoding;
import java.io.IOException;
import java.nio.charset.Charset;
import org.jspecify.annotations.Nullable;

public class EntityDefinitionDeserializer extends StdDeserializer<EntityDefinition> {

  public EntityDefinitionDeserializer() {
    super(EntityDefinition.class);
  }

  @Override
  public EntityDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = parser.readValueAsTree();

    if (node.isTextual()) {
      return new SimpleStringEntityDefinition(node.asText());
    }

    if (node.isObject()) {
      if (isJsonObject(node)) {
        return new JsonEntityDefinition(node.get("data"));
      }

      Format format = getFormat(node);
      Charset charset = getCharset(node);
      CompressionType compression = getCompression(node);
      String dataStore = getString(node, "dataStore");
      String dataRef = getString(node, "dataRef");
      String data = getString(node, "data");
      String base64Data = getString(node, "base64Data");
      String filePath = getString(node, "filePath");

      return new EntityDefinition(
          compression,
          resolveFormat(format, data, base64Data),
          charset,
          buildDataRef(dataStore, dataRef),
          resolveData(data, base64Data, charset),
          filePath);
    }

    return null;
  }

  private static boolean isJsonObject(JsonNode node) {
    final JsonNode dataNode = node.get("data");
    return dataNode != null && dataNode.isObject();
  }

  private static Format getFormat(JsonNode node) {
    JsonNode formatNode = node.get("format");
    if (formatNode == null) {
      formatNode = node.get("encoding");
    }
    if (formatNode != null && formatNode.isTextual()) {
      return Format.fromString(formatNode.asText());
    }
    return null;
  }

  private static Charset getCharset(JsonNode node) {
    JsonNode charsetNode = node.get("charset");
    if (charsetNode != null && charsetNode.isTextual()) {
      return Charset.forName(charsetNode.asText());
    }
    return null;
  }

  private static CompressionType getCompression(JsonNode node) {
    JsonNode compressionNode = node.get("compression");
    if (compressionNode != null && compressionNode.isTextual()) {
      return CompressionType.fromString(compressionNode.asText());
    }
    return null;
  }

  private static String getString(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode != null && fieldNode.isTextual()) {
      return fieldNode.asText();
    }
    return null;
  }

  private static Format resolveFormat(Format format, String data, String base64Data) {
    if (format != null) {
      return format;
    }

    if (base64Data != null) {
      return Format.BINARY;
    }

    if (data != null) {
      return detectFormat(data);
    }

    return EntityDefinition.DEFAULT_FORMAT;
  }

  private static @Nullable DataStoreRef buildDataRef(
      @Nullable String dataStore, @Nullable String dataRef) {
    if (dataStore == null || dataRef == null) {
      return null;
    } else {
      return new DataStoreRef(dataStore, dataRef);
    }
  }

  private static byte[] resolveData(String data, String base64Data, Charset charset) {
    if (data != null) {
      return bytesFromString(data, getFirstNonNull(charset, DEFAULT_CHARSET));
    }

    if (base64Data != null) {
      return Encoding.decodeBase64(base64Data);
    }

    return null;
  }
}
