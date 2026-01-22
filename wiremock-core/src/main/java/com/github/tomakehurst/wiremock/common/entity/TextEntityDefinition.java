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
import static com.github.tomakehurst.wiremock.common.entity.EncodingType.TEXT;
import static com.github.tomakehurst.wiremock.common.entity.TextFormat.HTML;
import static com.github.tomakehurst.wiremock.common.entity.TextFormat.JSON;
import static com.github.tomakehurst.wiremock.common.entity.TextFormat.XML;
import static com.github.tomakehurst.wiremock.common.entity.TextFormat.YAML;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

@JsonDeserialize(as = TextEntityDefinition.class)
public class TextEntityDefinition extends EntityDefinition<TextEntityDefinition> {

  public static final TextFormat DEFAULT_FORMAT = TextFormat.TEXT;

  @NonNull private final TextFormat format;
  @NonNull protected final Charset charset;
  private final String dataStore;
  private final String dataRef;
  private final byte[] data;
  private final String filePath;

  public static TextEntityDefinition full(String text) {
    return (TextEntityDefinition) builder().setEncoding(TEXT).setData(text).build();
  }

  public static TextEntityDefinition simple(String text) {
    return new SimpleStringEntityDefinition(text);
  }

  public static TextEntityDefinition json(Object data) {
    return new JsonEntityDefinition(data);
  }

  public TextEntityDefinition(
      @JsonProperty("format") TextFormat format,
      @JsonProperty("charset") Charset charset,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") Object data,
      @JsonProperty("filePath") String filePath) {

    super(compression);

    assertValidParameterCombination(data, filePath, dataStore, dataRef);

    this.charset = getFirstNonNull(charset, DEFAULT_CHARSET);
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = resolveData(data);
    this.filePath = filePath;

    if (asList(TextFormat.values()).contains(format)) {
      this.format = format;
    } else if (data instanceof String s) {
      this.format = detectFormat(s);
    } else if (this.data != null) {
      this.format = detectFormat(Strings.stringFromBytes(this.data, this.charset));
    } else {
      this.format = DEFAULT_FORMAT;
    }
  }

  private TextFormat detectFormat(String data) {
    if (data == null || data.isEmpty()) {
      return TextFormat.TEXT;
    }

    String trimmed = data.trim();
    if (trimmed.isEmpty()) {
      return TextFormat.TEXT;
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

    return TextFormat.TEXT;
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
    return TEXT;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultFormatFilter.class)
  public TextFormat getFormat() {
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

  public String getFilePath() {
    return filePath;
  }

  public TextEntityDefinition withCharset(Charset charset) {
    return (TextEntityDefinition) transform(builder -> builder.setCharset(charset));
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

  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public static class DefaultFormatFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_FORMAT.equals(obj);
    }
  }

  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public static class DefaultCharsetFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_CHARSET.equals(obj);
    }
  }

  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public static class DefaultCompressionFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_COMPRESSION.equals(obj);
    }
  }

  //  public static class Builder extends EntityDefinition.BaseBuilder<Builder,
  // TextEntityDefinition> {
  //    protected TextFormat format;
  //    protected Charset charset = DEFAULT_CHARSET;
  //    protected byte[] data;
  //
  //    public Builder(TextEntityDefinition entity) {
  //      super(entity.compression, entity.dataStore, entity.dataRef, entity.filePath);
  //      this.format = entity.format;
  //      this.charset = entity.charset;
  //      this.data = entity.data;
  //    }
  //
  //    public Builder() {}
  //
  //    public Builder setFormat(TextFormat format) {
  //      this.format = format;
  //      return this;
  //    }
  //
  //    public Builder setCharset(Charset charset) {
  //      this.charset = charset;
  //      return this;
  //    }
  //
  //    public Builder setData(Object data) {
  //      resetDataAndRefs();
  //      this.data =
  //          data instanceof String s
  //              ? Strings.bytesFromString(s, charset)
  //              : data instanceof byte[] b ? b : null;
  //      return this;
  //    }
  //
  //    @Override
  //    protected void resetDataAndRefs() {
  //      super.resetDataAndRefs();
  //      this.data = null;
  //    }
  //
  //    @Override
  //    public TextEntityDefinition build() {
  //      return new TextEntityDefinition(
  //          format, charset, compression, dataStore, dataRef, data, filePath);
  //    }
  //  }
}
