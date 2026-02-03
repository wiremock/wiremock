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
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.ObjectStore;
import com.github.tomakehurst.wiremock.store.Stores;

public class EntityResolver {

  private final Stores stores;

  public EntityResolver(Stores stores) {
    this.stores = stores;
  }

  public Entity resolve(EntityDefinition definition) {
    if (definition instanceof EmptyEntityDefinition) {
      return Entity.EMPTY;
    }

    InputStreamSource bodySource = resolveEntityData(definition);
    final Entity.Builder builder =
        Entity.builder()
            .setCompression(definition.getCompression())
            .setFormat(definition.getFormat())
            .setCharset(definition.getCharset())
            .setDataStreamSource(bodySource);

    return builder.build();
  }

  private InputStreamSource resolveEntityData(EntityDefinition definition) {
    if (definition.isInline()) {
      return StreamSources.forBytes(definition.getDataAsBytes());
    }

    if (definition instanceof FilePathEntityDefinition filePathEntityDefinition && stores != null) {
      BlobStore filesBlobStore = stores.getFilesBlobStore();
      return filesBlobStore.getStreamSource(filePathEntityDefinition.getFilePath());
    }

    if (definition instanceof DataRefEntityDefinition dataRefEntityDefinition && stores != null) {
      DataStoreRef dataStoreRef = dataRefEntityDefinition.getDataStoreRef();
      BlobStore blobStore = stores.getBlobStore(dataStoreRef.store());
      if (blobStore != null && blobStore.contains(dataStoreRef.key())) {
        return blobStore.getStreamSource(dataStoreRef.key());
      }

      ObjectStore objectStore = stores.getObjectStore(dataStoreRef.store());
      if (objectStore != null && objectStore.contains(dataStoreRef.key())) {
        return objectStore
            .get(dataStoreRef.key())
            .map(data -> StreamSources.forBytes(Json.toByteArray(data)))
            .orElse(null);
      }
    }

    return null;
  }
}
