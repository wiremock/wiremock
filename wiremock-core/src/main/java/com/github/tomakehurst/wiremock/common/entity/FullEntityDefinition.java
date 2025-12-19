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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Objects;

public class FullEntityDefinition extends EntityDefinition {
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
      @JsonProperty("dataStore") String dataStore, // validate and needs to be templatable
      @JsonProperty("dataRef") String dataRef, // needs to be templatable
      @JsonProperty("data") Object data) {
    // default the encoding to text if not specified or not valid
    this.encoding = asList(EncodingType.values()).contains(encoding) ? encoding : EncodingType.TEXT;
    // default the format to text if not specified or not valid
    this.format = asList(FormatType.values()).contains(format) ? format : FormatType.TEXT;
    // default the compression to none if not specified or not valid
    this.compression =
        asList(CompressionType.values()).contains(compression) ? compression : CompressionType.NONE;
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = data;

    // TODO: do we want to override the format based on what we know we have parsed?
  }

  public EncodingType getEncoding() {
    return encoding;
  }

  public FormatType getFormat() {
    return format;
  }

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
}
