/*
 * Copyright (C) 2014-2025 Thomas Akehurst
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

import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Base64;
import java.util.Objects;

@JsonDeserialize(as = BinaryEntityDefinition.class)
public class BinaryEntityDefinition extends EntityDefinition {

  public static final CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;

  private final CompressionType compression;
  private final String dataStore;
  private final String dataRef;
  private final String data;
  private final String filePath;

  public BinaryEntityDefinition(
      @JsonProperty("encoding") EncodingType ignored,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") String data,
      @JsonProperty("filePath") String filePath) {
    // encoding is accepted for deserialization but ignored (always BINARY)
    this.compression =
        asList(CompressionType.values()).contains(compression) ? compression : DEFAULT_COMPRESSION;
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = data;
    this.filePath = filePath;
  }

  public static Builder aBinaryMessage() {
    return new Builder();
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

  public String getDataStore() {
    return dataStore;
  }

  public String getDataRef() {
    return dataRef;
  }

  @Override
  public String getData() {
    return data;
  }

  @JsonIgnore
  public byte[] getDataAsBytes() {
    if (data == null) {
      return null;
    }
    return Base64.getDecoder().decode(data);
  }

  public String getFilePath() {
    return filePath;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    BinaryEntityDefinition that = (BinaryEntityDefinition) o;
    return Objects.equals(compression, that.compression)
        && Objects.equals(dataStore, that.dataStore)
        && Objects.equals(dataRef, that.dataRef)
        && Objects.equals(data, that.data)
        && Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compression, dataStore, dataRef, data, filePath);
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
    private String data;
    private String filePath;

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
      this.data = data != null ? Base64.getEncoder().encodeToString(data) : null;
      return this;
    }

    public Builder withBodyBase64(String base64Data) {
      this.data = base64Data;
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
