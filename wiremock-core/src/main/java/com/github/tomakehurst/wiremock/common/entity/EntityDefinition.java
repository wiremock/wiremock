/*
 * Copyright (C) 2014-2026 Thomas Akehurst
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.nio.charset.Charset;
import java.util.function.Consumer;

@JsonInclude(NON_NULL)
@JsonDeserialize(using = EntityDefinitionDeserializer.class)
@JsonSubTypes(
    value = {
      @JsonSubTypes.Type(TextEntityDefinition.class),
      @JsonSubTypes.Type(SimpleStringEntityDefinition.class),
      @JsonSubTypes.Type(JsonEntityDefinition.class),
      @JsonSubTypes.Type(BinaryEntityDefinition.class)
    })
public abstract class EntityDefinition<SELF extends EntityDefinition<SELF>> {

  public static final Charset DEFAULT_CHARSET = UTF_8;
  public static final CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;

  protected static void assertValidParameterCombination(
      Object data, String filePath, String dataStore, String dataRef) {
    if (data != null && filePath != null) {
      throw new IllegalArgumentException("Cannot specify an entity with both data and filePath");
    }
    if (data != null && dataRef != null) {
      throw new IllegalArgumentException(
          "Cannot specify an entity with both data and data store reference");
    }
    if (filePath != null && dataStore != null) {
      throw new IllegalArgumentException(
          "Cannot specify an entity with both filePath and data store reference");
    }
  }

  public abstract EncodingType getEncoding();

  public abstract FormatType getFormat();

  public abstract CompressionType getCompression();

  public abstract Object getData();

  @JsonIgnore
  public boolean isInline() {
    return getData() != null;
  }

  @JsonIgnore
  public boolean isFromFile() {
    return getFilePath() != null;
  }

  @JsonIgnore
  public abstract String getDataAsString();

  @JsonIgnore
  public abstract byte[] getDataAsBytes();

  @JsonIgnore
  public boolean isDecompressable() {
    final CompressionType compression = getCompression();
    return compression == NONE || compression == GZIP;
  }

  public abstract SELF decompress();

  public String getFilePath() {
    if (FILES_ROOT.equals(getDataStore()) && getDataRef() != null) {
      return getDataRef();
    }

    return null;
  }

  public abstract String getDataStore();

  public abstract String getDataRef();

  public abstract <B extends Builder<SELF>> SELF transform(Consumer<B> transformer);

  public interface Builder<T extends EntityDefinition<T>> {

    Builder<T> setCompression(CompressionType compression);

    Builder<T> setDataStoreRef(String storeName, String key);

    Builder<T> setFilePath(String filePath);

    T build();
  }

  public abstract static class BaseBuilder<SELF extends Builder<T>, T extends EntityDefinition<T>>
      implements Builder<T> {

    protected CompressionType compression = NONE;
    protected String dataStore;
    protected String dataRef;
    protected String filePath;

    public BaseBuilder() {}

    protected BaseBuilder(
        CompressionType compression, String dataStore, String dataRef, String filePath) {
      this.compression = compression;
      this.dataStore = dataStore;
      this.dataRef = dataRef;
      this.filePath = filePath;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF setCompression(CompressionType compression) {
      this.compression = compression;
      return (SELF) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF setFilePath(String filePath) {
      resetDataAndRefs();
      this.filePath = filePath;
      return (SELF) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF setDataStoreRef(String storeName, String key) {
      resetDataAndRefs();
      this.dataStore = storeName;
      this.dataRef = key;
      return (SELF) this;
    }

    protected void resetDataAndRefs() {
      this.dataStore = null;
      this.dataRef = null;
      this.filePath = null;
    }
  }
}
