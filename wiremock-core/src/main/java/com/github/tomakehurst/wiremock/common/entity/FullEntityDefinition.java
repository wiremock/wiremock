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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Objects;

@JsonDeserialize(as = FullEntityDefinition.class)
public class FullEntityDefinition extends EntityDefinition {

  public static final EncodingType DEFAULT_ENCODING = EncodingType.TEXT;
  public static final FormatType DEFAULT_FORMAT = FormatType.JSON;
  public static final CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;

  private final EncodingType encoding;
  private final FormatType format;
  private final CompressionType compression;
  private final String dataStore;
  private final String dataRef;
  private final Object data;

  public FullEntityDefinition(
      @JsonProperty("encoding") EncodingType encoding,
      @JsonProperty("format") FormatType format,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") Object data) {
    this.encoding = asList(EncodingType.values()).contains(encoding) ? encoding : DEFAULT_ENCODING;
    this.format = asList(FormatType.values()).contains(format) ? format : DEFAULT_FORMAT;
    this.compression =
        asList(CompressionType.values()).contains(compression) ? compression : DEFAULT_COMPRESSION;
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = data;
  }

  public static Builder aMessage() {
    return new Builder();
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultEncodingFilter.class)
  public EncodingType getEncoding() {
    return encoding;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultFormatFilter.class)
  public FormatType getFormat() {
    return format;
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

  public Object getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    FullEntityDefinition that = (FullEntityDefinition) o;
    return Objects.equals(encoding, that.encoding)
        && Objects.equals(format, that.format)
        && Objects.equals(compression, that.compression)
        && Objects.equals(dataStore, that.dataStore)
        && Objects.equals(dataRef, that.dataRef)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(encoding, format, compression, dataStore, dataRef, data);
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public static class DefaultEncodingFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_ENCODING.equals(obj);
    }
  }

  public static class DefaultFormatFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_FORMAT.equals(obj);
    }
  }

  public static class DefaultCompressionFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_COMPRESSION.equals(obj);
    }
  }

  public static class Builder {
    private EncodingType encoding;
    private FormatType format;
    private CompressionType compression;
    private String dataStore;
    private String dataRef;
    private Object data;

    public Builder withEncoding(EncodingType encoding) {
      this.encoding = encoding;
      return this;
    }

    public Builder withFormat(FormatType format) {
      this.format = format;
      return this;
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

    public Builder withBody(Object data) {
      this.data = data;
      return this;
    }

    public FullEntityDefinition build() {
      return new FullEntityDefinition(encoding, format, compression, dataStore, dataRef, data);
    }
  }
}
