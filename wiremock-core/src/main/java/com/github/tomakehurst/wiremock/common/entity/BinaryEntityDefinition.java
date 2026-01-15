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
public class BinaryEntityDefinition extends EntityDefinition {

  public static final CompressionType DEFAULT_COMPRESSION = NONE;

  private final CompressionType compression;
  private final String dataStore;
  private final String dataRef;
  private final byte[] data;
  private final String filePath;

  public static EntityDefinition fromBase64(String base64) {
    return new Builder().withBodyBase64(base64).build();
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

    return new byte[0];
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
      return transform(
          builder -> builder.withBody(Gzip.unGzip(getDataAsBytes())).withCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  public BinaryEntityDefinition transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
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

  public static class Builder implements EntityDefinition.Builder<BinaryEntityDefinition> {
    private CompressionType compression;
    private String dataStore;
    private String dataRef;
    private byte[] data;
    private String filePath;

    public Builder() {}

    public Builder(BinaryEntityDefinition entity) {
      this.compression = entity.compression;
      this.dataStore = entity.dataStore;
      this.dataRef = entity.dataRef;
      this.data = entity.data;
      this.filePath = entity.filePath;
    }

    public Builder withCompression(CompressionType compression) {
      this.compression = compression;
      return this;
    }

    public Builder withDataStore(String dataStore) {
      this.dataStore = dataStore;
      return this;
    }

    public Builder withDataRef(String dataRef) {
      this.dataRef = dataRef;
      return this;
    }

    public Builder withBody(byte[] data) {
      this.data = data;
      return this;
    }

    public Builder withBodyBase64(String base64Data) {
      this.data = Encoding.decodeBase64(base64Data);
      return this;
    }

    public Builder withFilePath(String filePath) {
      this.filePath = filePath;
      return this;
    }

    @Override
    public BinaryEntityDefinition build() {
      return new BinaryEntityDefinition(null, compression, dataStore, dataRef, data, filePath);
    }
  }
}
