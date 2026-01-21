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

import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

@JsonSerialize(as = BinaryEntityDefinition.class)
@JsonDeserialize(as = BinaryEntityDefinition.class)
public class BinaryEntityDefinition extends EntityDefinition<BinaryEntityDefinition> {

  public static final CompressionType DEFAULT_COMPRESSION = NONE;

  private final CompressionType compression;
  private final String dataStore;
  private final String dataRef;
  private final byte[] data;
  private final String filePath;

  public static EntityDefinition fromBase64(String base64) {
    return new Builder().setBodyBase64(base64).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public BinaryEntityDefinition(
      // encoding is accepted for deserialization but ignored (always BINARY)
      @JsonProperty("encoding") EncodingType ignored,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") Object data,
      @JsonProperty("filePath") String filePath) {

    assertValidParameterCombination(data, filePath, dataStore, dataRef);

    this.compression =
        asList(CompressionType.values()).contains(compression) ? compression : DEFAULT_COMPRESSION;
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = resolveData(data);
    this.filePath = filePath;
  }

  private byte[] resolveData(Object data) {
    if (data instanceof byte[] bytes) {
      return bytes;
    }

    if (data instanceof String s) {
      return Encoding.decodeBase64(s);
    }

    return null;
  }

  @Override
  public EncodingType getEncoding() {
    return EncodingType.BINARY;
  }

  @Override
  @JsonIgnore
  public FormatType getFormat() {
    return FormatType.BASE64;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultCompressionFilter.class)
  public CompressionType getCompression() {
    return compression;
  }

  @Override
  public String getDataStore() {
    return dataStore;
  }

  @Override
  public String getDataRef() {
    return dataRef;
  }

  @Override
  public byte[] getData() {
    return data;
  }

  @Override
  public String getDataAsString() {
    return Encoding.encodeBase64(data);
  }

  @Override
  public byte[] getDataAsBytes() {
    return data;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BinaryEntityDefinition decompress() {
    final CompressionType compression = getCompression();
    if (compression == GZIP) {
      return this.<Builder>transform(
          builder -> builder.setBody(Gzip.unGzip(getDataAsBytes())).setCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  @Override
  public <B extends EntityDefinition.Builder<BinaryEntityDefinition>>
      BinaryEntityDefinition transform(Consumer<B> transformer) {
    final Builder builder = toBuilder();
    @SuppressWarnings("unchecked")
    final B typedBuilder = (B) builder;
    transformer.accept(typedBuilder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    BinaryEntityDefinition that = (BinaryEntityDefinition) o;
    return Objects.equals(compression, that.compression)
        && Objects.equals(dataStore, that.dataStore)
        && Objects.equals(dataRef, that.dataRef)
        && Arrays.equals(data, that.data)
        && Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compression, dataStore, dataRef, Arrays.hashCode(data), filePath);
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public static class DefaultCompressionFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_COMPRESSION.equals(obj);
    }
  }

  public static class Builder
      extends EntityDefinition.BaseBuilder<Builder, BinaryEntityDefinition> {

    private byte[] data;

    public Builder() {}

    public Builder(BinaryEntityDefinition entity) {
      super(entity.compression, entity.dataStore, entity.dataRef, entity.filePath);
      this.data = entity.data;
    }

    public Builder setBody(byte[] data) {
      resetDataAndRefs();
      this.data = data;
      return this;
    }

    public Builder setBodyBase64(String base64Data) {
      resetDataAndRefs();
      this.data = Encoding.decodeBase64(base64Data);
      return this;
    }

    @Override
    protected void resetDataAndRefs() {
      super.resetDataAndRefs();
      this.data = null;
    }

    public BinaryEntityDefinition build() {
      return new BinaryEntityDefinition(null, compression, dataStore, dataRef, data, filePath);
    }
  }
}
