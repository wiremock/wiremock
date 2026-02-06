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
package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityMetadataBuilder;
import com.github.tomakehurst.wiremock.common.entity.Format;
import java.nio.charset.Charset;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BodyMetadata {

  private final @Nullable Format format;
  private final @Nullable CompressionType compression;
  private final @Nullable Charset charset;

  @JsonCreator
  public BodyMetadata(
      @JsonProperty("format") @Nullable Format format,
      @JsonProperty("compression") @Nullable CompressionType compression,
      @JsonProperty("charset") @Nullable Charset charset) {
    this.format = format;
    this.compression = compression;
    this.charset = charset;
  }

  public @Nullable Format getFormat() {
    return format;
  }

  @JsonInclude(
      value = JsonInclude.Include.CUSTOM,
      valueFilter = EntityDefinition.DefaultCompressionFilter.class)
  public @Nullable CompressionType getCompression() {
    return compression;
  }

  @JsonInclude(
      value = JsonInclude.Include.CUSTOM,
      valueFilter = EntityDefinition.DefaultCharsetFilter.class)
  public @Nullable Charset getCharset() {
    return charset;
  }

  public <T extends EntityMetadataBuilder<T>> void applyTo(T builder) {
    if (format != null) {
      builder.setFormat(format);
    }
    if (compression != null) {
      builder.setCompression(compression);
    }
    if (charset != null) {
      builder.setCharset(charset);
    }
  }
}
