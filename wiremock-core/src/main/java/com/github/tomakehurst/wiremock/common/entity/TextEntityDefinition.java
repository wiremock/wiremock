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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.HTML;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.JSON;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.TEXT;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.XML;
import static com.github.tomakehurst.wiremock.common.entity.FormatType.YAML;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

@JsonDeserialize(as = TextEntityDefinition.class)
public class TextEntityDefinition extends EntityDefinition<TextEntityDefinition> {

  public static final FormatType DEFAULT_FORMAT = TEXT;

  @NonNull private final FormatType format;
  @NonNull private final Charset charset;
  @NonNull private final CompressionType compression;
  private final String dataStore;
  private final String dataRef;
  private final byte[] data;
  private final String filePath;

  public static Builder builder() {
    return new Builder();
  }

  public static TextEntityDefinition full(String text) {
    return new Builder().setData(text).build();
  }

  public static TextEntityDefinition simple(String text) {
    return new SimpleStringEntityDefinition(text);
  }

  public static TextEntityDefinition json(Object data) {
    return new JsonEntityDefinition(data);
  }

  public TextEntityDefinition(
      @JsonProperty("format") FormatType format,
      @JsonProperty("charset") Charset charset,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") Object data,
      @JsonProperty("filePath") String filePath) {

    assertValidParameterCombination(data, filePath, dataStore, dataRef);

    this.charset = getFirstNonNull(charset, DEFAULT_CHARSET);
    this.compression =
        asList(CompressionType.values()).contains(compression) ? compression : DEFAULT_COMPRESSION;
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = resolveData(data);
    this.filePath = filePath;

    if (asList(FormatType.values()).contains(format)) {
      this.format = format;
    } else if (data instanceof String s) {
      this.format = detectFormat(s);
    } else if (this.data != null) {
      this.format = detectFormat(Strings.stringFromBytes(this.data, this.charset));
    } else {
      this.format = DEFAULT_FORMAT;
    }
  }

  private FormatType detectFormat(String data) {
    if (data == null || data.isEmpty()) {
      return TEXT;
    }

    String trimmed = data.trim();
    if (trimmed.isEmpty()) {
      return TEXT;
    }

    char firstChar = trimmed.charAt(0);

    if (firstChar == '{' || firstChar == '[') {
      return JSON;
    }

    if (firstChar == '<') {
      if (trimmed.regionMatches(true, 0, "<!doctype html", 0, 14)
          || trimmed.regionMatches(true, 0, "<html", 0, 5)) {
        return HTML;
      }
      return XML;
    }

    if (trimmed.contains(":")
        && (trimmed.contains("\n-") || trimmed.matches("(?s).*\\n\\s+-\\s+.*"))) {
      return YAML;
    }

    return TEXT;
  }

  private byte[] resolveData(Object data) {
    if (data instanceof byte[] b) {
      return b;
    }

    if (data instanceof String s) {
      return Strings.bytesFromString(s, this.charset);
    }

    return null;
  }

  @Override
  @JsonIgnore
  public EncodingType getEncoding() {
    return EncodingType.TEXT;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultFormatFilter.class)
  public FormatType getFormat() {
    return format;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultCharsetFilter.class)
  public Charset getCharset() {
    return charset;
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

  public Object getData() {
    return getDataAsString();
  }

  @Override
  public String getDataAsString() {
    return Strings.stringFromBytes(data, charset);
  }

  @Override
  public byte[] getDataAsBytes() {
    return data;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TextEntityDefinition decompress() {
    final CompressionType compression = getCompression();
    if (compression == GZIP) {
      return this.<Builder>transform(
          builder -> builder.setData(Gzip.unGzip(data)).setCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  public TextEntityDefinition withCharset(Charset charset) {
    return this.<Builder>transform(builder -> builder.setCharset(charset));
  }

  @Override
  public <B extends EntityDefinition.Builder<TextEntityDefinition>> TextEntityDefinition transform(
      Consumer<B> transformer) {
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
    TextEntityDefinition that = (TextEntityDefinition) o;
    return Objects.equals(format, that.format)
        && Objects.equals(charset, that.charset)
        && Objects.equals(compression, that.compression)
        && Objects.equals(dataStore, that.dataStore)
        && Objects.equals(dataRef, that.dataRef)
        && Arrays.equals(data, that.data)
        && Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        format, charset, compression, dataStore, dataRef, Arrays.hashCode(data), filePath);
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public static class DefaultFormatFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_FORMAT.equals(obj);
    }
  }

  public static class DefaultCharsetFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_CHARSET.equals(obj);
    }
  }

  public static class DefaultCompressionFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_COMPRESSION.equals(obj);
    }
  }

  public static class Builder extends EntityDefinition.BaseBuilder<Builder, TextEntityDefinition> {
    protected FormatType format;
    protected Charset charset = DEFAULT_CHARSET;
    protected byte[] data;

    public Builder(TextEntityDefinition entity) {
      super(entity.compression, entity.dataStore, entity.dataRef, entity.filePath);
      this.format = entity.format;
      this.charset = entity.charset;
      this.data = entity.data;
    }

    public Builder() {}

    public Builder setFormat(FormatType format) {
      this.format = format;
      return this;
    }

    public Builder setCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

    public Builder setData(Object data) {
      resetDataAndRefs();
      this.data =
          data instanceof String s
              ? Strings.bytesFromString(s, charset)
              : data instanceof byte[] b ? b : null;
      return this;
    }

    @Override
    protected void resetDataAndRefs() {
      super.resetDataAndRefs();
      this.data = null;
    }

    @Override
    public TextEntityDefinition build() {
      return new TextEntityDefinition(
          format, charset, compression, dataStore, dataRef, data, filePath);
    }
  }
}
