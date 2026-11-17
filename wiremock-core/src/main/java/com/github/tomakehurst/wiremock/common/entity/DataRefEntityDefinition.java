/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.StreamSources;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.ObjectStore;
import com.github.tomakehurst.wiremock.store.Stores;
import java.nio.charset.Charset;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DataRefEntityDefinition extends EntityDefinition {

  private final @NonNull DataStoreRef dataStoreRef;

  DataRefEntityDefinition(
      CompressionType compression,
      Format format,
      Charset charset,
      @NonNull DataStoreRef dataStoreRef) {
    super(compression, format, charset);
    this.dataStoreRef = dataStoreRef;
  }

  @Override
  @Nullable InputStreamSource resolveEntityData(@Nullable Stores stores) {

    if (stores != null) {
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

  @Override
  public String getFilePath() {
    if (FILES_ROOT.equals(dataStoreRef.store())) {
      return dataStoreRef.key();
    }
    return null;
  }

  @Override
  public EntityDefinition.Builder toBuilder() {
    return new EntityDefinition.Builder(
        this.compression, this.format, this.charset, null, null, this.dataStoreRef, null, false);
  }

  @SuppressWarnings("unused")
  public @NonNull String getDataStore() {
    return dataStoreRef.store();
  }

  @SuppressWarnings("unused")
  public @NonNull String getDataRef() {
    return dataStoreRef.key();
  }

  @JsonIgnore
  public @NonNull DataStoreRef getDataStoreRef() {
    return dataStoreRef;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DataRefEntityDefinition that)) {
      return false;
    }
    return Objects.equals(compression, that.compression)
        && Objects.equals(format, that.format)
        && Objects.equals(charset, that.charset)
        && Objects.equals(dataStoreRef, that.dataStoreRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compression, format, charset, dataStoreRef);
  }
}
