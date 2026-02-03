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
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.common.entity.Format.BINARY;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.store.Stores;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@JsonInclude(NON_NULL)
@JsonDeserialize(using = EntityDefinitionDeserializer.class)
@JsonSubTypes(
    value = {
      @JsonSubTypes.Type(SimpleStringEntityDefinition.class),
      @JsonSubTypes.Type(JsonEntityDefinition.class)
    })
public abstract class EntityDefinition {

  public static final @NonNull Charset DEFAULT_CHARSET = UTF_8;
  public static final @NonNull CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;
  public static final @NonNull Format DEFAULT_FORMAT = Format.TEXT;

  protected final @NonNull CompressionType compression;
  protected final @Nullable Format format;
  protected final @Nullable Charset charset;

  EntityDefinition(
      @NonNull CompressionType compression, @Nullable Format format, @Nullable Charset charset) {
    this.compression = compression;
    this.charset = getFirstNonNull(charset, DEFAULT_CHARSET);
    this.format = format;
  }

  public static EntityDefinition full(String text) {
    return builder().setFormat(Format.TEXT).setData(text).build();
  }

  public static EntityDefinition simple(@Nullable String text) {
    return text != null ? new SimpleStringEntityDefinition(text) : EmptyEntityDefinition.INSTANCE;
  }

  public static JsonEntityDefinition json(Object data) {
    return new JsonEntityDefinition(data);
  }

  public static EntityDefinition fromBase64(String base64) {
    return builder().setFormat(Format.BINARY).setBodyBase64(base64).build();
  }

  private static CompressionType tryToGuessCompressionTypeIfNotSpecified(
      CompressionType compression, byte[] data) {
    if (compression != null) {
      return compression;
    }

    return data != null && Gzip.isGzipped(data) ? GZIP : DEFAULT_COMPRESSION;
  }

  private static void assertValidParameterCombination(
      @Nullable Object data, @Nullable String filePath, @Nullable DataStoreRef dataStoreRef) {
    if (data != null && filePath != null) {
      throw new IllegalArgumentException("Cannot specify an entity with both data and filePath");
    }
    if (data != null && dataStoreRef != null) {
      throw new IllegalArgumentException(
          "Cannot specify an entity with both data and data store reference");
    }
    if (filePath != null && dataStoreRef != null) {
      throw new IllegalArgumentException(
          "Cannot specify an entity with both filePath and data store reference");
    }
  }

  @NonNull Entity resolve(@Nullable Stores stores) {
    InputStreamSource bodySource = resolveEntityData(stores);
    final Entity.Builder builder =
        Entity.builder()
            .setCompression(getCompression())
            .setFormat(getFormat())
            .setCharset(getCharset())
            .setDataStreamSource(bodySource);

    return builder.build();
  }

  abstract @Nullable InputStreamSource resolveEntityData(@Nullable Stores stores);

  @JsonIgnore
  public boolean isAbsent() {
    return this instanceof EmptyEntityDefinition;
  }

  @NonNull
  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultCompressionFilter.class)
  public CompressionType getCompression() {
    return compression;
  }

  @JsonProperty("format")
  public Format getFormatForSerialization() {
    if (isInline() && (format == BINARY || format == DEFAULT_FORMAT)) {
      return null;
    }

    return format;
  }

  @JsonIgnore
  public @Nullable Format getFormat() {
    return format;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultCharsetFilter.class)
  public @Nullable Charset getCharset() {
    return charset;
  }

  public @Nullable Object getData() {
    return null;
  }

  @JsonIgnore
  public boolean isInline() {
    return getDataAsBytes() != null;
  }

  @JsonIgnore
  public boolean isFromFile() {
    return getFilePath() != null;
  }

  @JsonIgnore
  public boolean isBinary() {
    return format == Format.BINARY;
  }

  @JsonIgnore
  public boolean isCompressed() {
    return compression != NONE;
  }

  @JsonIgnore
  public String getDataAsString() {
    return null;
  }

  @JsonIgnore
  public byte[] getDataAsBytes() {
    return null;
  }

  @JsonIgnore
  public boolean isDecompressable() {
    final CompressionType compression = getCompression();
    return compression == NONE || compression == GZIP;
  }

  public @Nullable String getFilePath() {
    return null;
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public EntityDefinition transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public EntityDefinition decompress() {
    final CompressionType compression = getCompression();
    if (compression == GZIP) {
      final Format format = getFormat();
      return transform(
          builder ->
              builder
                  .setData(Gzip.unGzip(getDataAsBytes()))
                  .setFormat(format)
                  .setCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return this;
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder implements EntityMetadataBuilder<Builder> {

    private CompressionType compression;

    protected Format format;
    protected Charset charset = DEFAULT_CHARSET;

    private byte[] data;
    private Object jsonData;

    private @Nullable DataStoreRef dataStoreRef;
    private String filePath;

    private boolean v3Style = false;

    public Builder() {}

    public Builder(
        CompressionType compression,
        Format format,
        Charset charset,
        byte[] data,
        @Nullable Object jsonData,
        @Nullable DataStoreRef dataStoreRef,
        @Nullable String filePath,
        boolean v3Style) {
      this.compression = compression;
      this.format = format;
      this.charset = charset;
      this.data = data;
      this.jsonData = jsonData;
      this.dataStoreRef = dataStoreRef;
      this.filePath = filePath;
      this.v3Style = v3Style;
    }

    @SuppressWarnings("unused")
    public CompressionType getCompression() {
      return compression;
    }

    public Builder setCompression(CompressionType compression) {
      this.compression = compression;
      return this;
    }

    public Format getFormat() {
      return format;
    }

    public Builder setFormat(Format format) {
      this.format = format;
      return this;
    }

    public Charset getCharset() {
      return charset;
    }

    public Builder setCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

    public byte[] getData() {
      return data;
    }

    public Builder setData(String data) {
      resetDataAndRefs();
      this.data = bytesFromString(data, getFirstNonNull(charset, DEFAULT_CHARSET));
      return this;
    }

    public Builder setBodyBase64(String base64Data) {
      resetDataAndRefs();
      this.data = Encoding.decodeBase64(base64Data);
      this.format = BINARY;
      return this;
    }

    @SuppressWarnings("unused")
    public Builder setJsonData(Object data) {
      resetDataAndRefs();
      this.jsonData = data instanceof JsonNode ? (JsonNode) data : Json.node(data);
      return this;
    }

    public Builder setData(byte[] data) {
      resetDataAndRefs();
      this.data = data;
      if (this.format == null) {
        this.format = BINARY;
      }
      return this;
    }

    @SuppressWarnings("unused")
    public @Nullable DataStoreRef getDataStoreRef() {
      return dataStoreRef;
    }

    public Builder setDataStoreRef(@NonNull String storeName, @NonNull String key) {
      resetDataAndRefs();
      this.dataStoreRef = new DataStoreRef(storeName, key);
      return this;
    }

    public String getFilePath() {
      return filePath;
    }

    public Builder setFilePath(String filePath) {
      resetDataAndRefs();
      this.filePath = filePath;
      return this;
    }

    protected void resetDataAndRefs() {
      this.data = null;
      this.jsonData = null;
      this.dataStoreRef = null;
      this.filePath = null;
    }

    public EntityDefinition build() {
      if (jsonData != null) {
        return new JsonEntityDefinition(jsonData);
      }

      if (v3Style) {
        return new SimpleStringEntityDefinition(data, charset);
      }

      return buildEntityDefinition(compression, format, charset, dataStoreRef, data, filePath);
    }
  }

  public static @NonNull EntityDefinition buildEntityDefinition(
      @Nullable CompressionType compression,
      @Nullable Format format,
      @Nullable Charset charset,
      @Nullable DataStoreRef dataStoreRef,
      byte[] data,
      @Nullable String filePath) {

    assertValidParameterCombination(data, filePath, dataStoreRef);

    CompressionType correctCompression = tryToGuessCompressionTypeIfNotSpecified(compression, data);
    Charset correctCharset = getFirstNonNull(charset, DEFAULT_CHARSET);
    Format correctFormat;
    if (format == null && data != null) {
      correctFormat = Format.detectFormat(stringFromBytes(data, correctCharset));
    } else {
      correctFormat = getFirstNonNull(format, DEFAULT_FORMAT);
    }

    if (data != null) {
      return new SimpleEntityDefinition(correctCompression, correctFormat, correctCharset, data);
    } else if (dataStoreRef != null) {
      return new DataRefEntityDefinition(
          correctCompression, correctFormat, correctCharset, dataStoreRef);
    } else if (filePath != null) {
      return new FilePathEntityDefinition(
          correctCompression, correctFormat, correctCharset, filePath);
    } else {
      return EmptyEntityDefinition.INSTANCE;
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
}
