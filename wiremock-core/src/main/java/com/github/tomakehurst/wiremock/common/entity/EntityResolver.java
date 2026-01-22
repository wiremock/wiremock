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

import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.StreamSources;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.Stores;

import static java.util.Base64.getDecoder;

public class EntityResolver {

  private final Stores stores;

  public EntityResolver(Stores stores) {
    this.stores = stores;
  }

  public Entity resolve(EntityDefinition<?> definition) {
    if (definition instanceof EmptyEntityDefinition) {
      return Entity.EMPTY;
    }

    InputStreamSource bodySource = resolveEntityData(definition);
    final Entity.Builder builder = Entity.builder()
            .setEncoding(definition.getEncoding())
            .setCompression(definition.getCompression())
            .setFormat(definition.getFormat())
            .setDataStreamSource(bodySource);

    if (definition instanceof TextEntityDefinition textEntityDefinition) {
      builder.setCharset(textEntityDefinition.getCharset());
    }

    return builder.build();
  }

  private InputStreamSource resolveEntityData(EntityDefinition<?> definition) {
    if (definition.isInline()) {
      return StreamSources.forBytes(definition.getDataAsBytes());
    }

    String filePath = definition.getFilePath();
    if (filePath != null && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.getStreamSource(filePath);
    }

    String dataStore = definition.getDataStore();
    String dataRef = definition.getDataRef();
    if (dataStore != null && dataRef != null && stores != null) {
      return stores
              .getBlobStore(dataStore)
              .getStreamSource(dataRef);
    }

    return null;
  }
}
