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

import static java.util.Base64.getDecoder;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.Stores;

public class EntityResolver {

  private final Stores stores;

  public EntityResolver(Stores stores) {
    this.stores = stores;
  }

  public Entity resolve(EntityDefinition<?> definition) {
    if (definition instanceof BinaryEntityDefinition binaryDef) {
      byte[] bytes = resolveBinaryEntityData(binaryDef);

      return Entity.builder()
          .setEncoding(EncodingType.BINARY)
          .setFormat(FormatType.BASE64)
          .setCompression(binaryDef.getCompression())
          .setBody(bytes)
          .build();
    }

    if (definition instanceof TextEntityDefinition textDef) {
      String resolvedData = resolveTextEntityData(textDef);
      return Entity.builder()
          .setEncoding(EncodingType.TEXT)
          .setFormat(textDef.getFormat())
          .setCharset(textDef.getCharset())
          .setCompression(textDef.getCompression())
          .setBody(resolvedData)
          .build();
    }

    return Entity.EMPTY;
  }

  private String resolveTextEntityData(TextEntityDefinition definition) {
    if (definition.isInline()) {
      return definition.getDataAsString();
    }

    String filePath = definition.getFilePath();
    if (filePath != null && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.get(filePath).map(Strings::stringFromBytes).orElse(null);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
          .getObjectStore(dataStore)
          .get(dataRef)
          .map(
              value -> {
                if (value instanceof String s) {
                  return s;
                }
                return Json.write(value);
              })
          .orElse(null);
    }

    return null;
  }

  private byte[] resolveBinaryEntityData(BinaryEntityDefinition definition) {
    byte[] data = definition.getDataAsBytes();
    if (data != null) {
      return data;
    }

    String filePath = definition.getFilePath();
    if (filePath != null && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.get(filePath).orElse(null);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
          .getObjectStore(dataStore)
          .get(dataRef)
          .map(
              value -> {
                if (value instanceof byte[] bytes) {
                  return bytes;
                }
                if (value instanceof String s) {
                  return getDecoder().decode(s);
                }
                return new byte[0];
              })
          .orElse(new byte[0]);
    }

    return new byte[0];
  }
}
