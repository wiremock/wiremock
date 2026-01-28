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
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

@JsonInclude(NON_NULL)
@JsonDeserialize(using = EntityDefinitionDeserializer.class)
@JsonSubTypes(
    value = {
      @JsonSubTypes.Type(SimpleStringEntityDefinition.class),
      @JsonSubTypes.Type(JsonEntityDefinition.class)
    })
public class EntityDefinition {

  public static final Charset DEFAULT_CHARSET = UTF_8;
  public static final CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;
  public static final Format DEFAULT_FORMAT = Format.TEXT;

  @NonNull protected final CompressionType compression;
  private final Format format;
  protected final Charset charset;
  private final String dataStore;
  private final String dataRef;
  private final byte[] data;
  private final String filePath;

  public EntityDefinition(
      @JsonProperty("format") Format format,
      @JsonProperty("charset") Charset charset,
      @JsonProperty("compression") CompressionType compression,
      @JsonProperty("dataStore") String dataStore,
      @JsonProperty("dataRef") String dataRef,
      @JsonProperty("data") String data,
      @JsonProperty("base64Data") String base64Data,
      @JsonProperty("filePath") String filePath) {

    this(
        compression,
        resolveFormat(format, data, base64Data),
        getFirstNonNull(charset, DEFAULT_CHARSET),
        dataStore,
        dataRef,
        resolveData(data, base64Data, charset),
        filePath);
  }

  EntityDefinition(
      CompressionType compression,
      Format format,
      Charset charset,
      String dataStore,
      String dataRef,
      byte[] data,
      String filePath) {
    this.compression = getFirstNonNull(compression, DEFAULT_COMPRESSION);
    this.charset = getFirstNonNull(charset, DEFAULT_CHARSET);
    if (format == null && data != null) {
      this.format = Format.detectFormat(stringFromBytes(data, this.charset));
    } else {
      this.format = getFirstNonNull(format, DEFAULT_FORMAT);
    }
    this.dataStore = dataStore;
    this.dataRef = dataRef;
    this.data = data;
    this.filePath = filePath;

    assertValidParameterCombination(this.data, this.filePath, this.dataStore, this.dataRef);
  }

  private static Format resolveFormat(Format format, String data, String base64Data) {
    if (format != null) {
      return format;
    }

    if (base64Data != null) {
      return Format.BINARY;
    }

    if (data != null) {
      return Format.detectFormat(data);
    }

    return DEFAULT_FORMAT;
  }

  private static byte[] resolveData(String data, String base64Data, Charset charset) {
    if (data != null) {
      return bytesFromString(data, getFirstNonNull(charset, DEFAULT_CHARSET));
    }

    if (base64Data != null) {
      return Encoding.decodeBase64(base64Data);
    }

    return null;
  }

  public static EntityDefinition full(String text) {
    return builder().setFormat(Format.TEXT).setData(text).build();
  }

  public static SimpleStringEntityDefinition simple(String text) {
    return new SimpleStringEntityDefinition(text);
  }

  public static JsonEntityDefinition json(Object data) {
    return new JsonEntityDefinition(data);
  }

  public static EntityDefinition fromBase64(String base64) {
    return builder().setFormat(Format.BINARY).setBodyBase64(base64).build();
  }

  private static CompressionType tryToGuessCompressionType(Object data) {
    if (data instanceof byte[] bytes) {
      return Gzip.isGzipped(bytes) ? GZIP : NONE;
    }
    if (data instanceof String s) {
      byte[] bytes = Encoding.decodeBase64(s);
      return Gzip.isGzipped(bytes) ? GZIP : NONE;
    }
    return null;
  }

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
    if (format == BINARY || format == DEFAULT_FORMAT) {
      return null;
    }

    return format;
  }

  @JsonIgnore
  public Format getFormat() {
    return format;
  }

  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultCharsetFilter.class)
  public Charset getCharset() {
    return charset;
  }

  public Object getData() {
    if (!isBinary() && !isCompressed()) {
      return stringFromBytes(data, charset);
    }

    return null;
  }

  public String getBase64Data() {
    if (isBinary() || isCompressed()) {
      return Encoding.encodeBase64(data);
    }

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
    if (format != Format.BINARY) {
      return stringFromBytes(data, charset);
    } else {
      return getBase64Data();
    }
  }

  @JsonIgnore
  public byte[] getDataAsBytes() {
    return data;
  }

  @JsonIgnore
  public boolean isDecompressable() {
    final CompressionType compression = getCompression();
    return compression == NONE || compression == GZIP;
  }

  public String getFilePath() {
    if (filePath != null) {
      return filePath;
    }
    if (FILES_ROOT.equals(getDataStore()) && getDataRef() != null) {
      return getDataRef();
    }
    return null;
  }

  public String getDataStore() {
    return dataStore;
  }

  public String getDataRef() {
    return dataRef;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    EntityDefinition that = (EntityDefinition) o;
    return Objects.equals(compression, that.compression)
        && Objects.equals(format, that.format)
        && Objects.equals(charset, that.charset)
        && Objects.equals(dataStore, that.dataStore)
        && Objects.equals(dataRef, that.dataRef)
        && Objects.deepEquals(data, that.data)
        && Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        compression, format, charset, dataStore, dataRef, Arrays.hashCode(data), filePath);
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

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder implements EntityMetadataBuilder<Builder> {

    private CompressionType compression = NONE;

    protected Format format;
    protected Charset charset = DEFAULT_CHARSET;

    private byte[] data;
    private Object jsonData;

    private String dataStore;
    private String dataRef;
    private String filePath;

    private boolean v3Style = false;

    public Builder() {}

    public Builder(EntityDefinition entity) {
      this.compression = entity.getCompression();
      this.format = entity.getFormat();
      this.charset = entity.getCharset();

      if (entity instanceof JsonEntityDefinition jsonEntity) {
        this.jsonData = jsonEntity.getDataAsJson();
      } else {
        this.data = entity.getDataAsBytes();
        this.dataStore = entity.getDataStore();
        this.dataRef = entity.getDataRef();
        this.filePath = entity.getFilePath();
      }

      if (entity instanceof SimpleStringEntityDefinition) {
        this.v3Style = true;
      }
    }

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

    public String getDataStore() {
      return dataStore;
    }

    public String getDataRef() {
      return dataRef;
    }

    public Builder setDataStoreRef(String storeName, String key) {
      resetDataAndRefs();
      this.dataStore = storeName;
      this.dataRef = key;
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
      this.dataStore = null;
      this.dataRef = null;
      this.filePath = null;
    }

    public EntityDefinition build() {
      if (jsonData != null) {
        return new JsonEntityDefinition(jsonData);
      }

      if (v3Style) {
        return new SimpleStringEntityDefinition(stringFromBytes(data, charset));
      }

      return new EntityDefinition(compression, format, charset, dataStore, dataRef, data, filePath);
    }
  }

  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public static class DefaultFormatFilter {
    @Override
    public boolean equals(Object obj) {
      return DEFAULT_FORMAT.equals(obj);
    }
  }

  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public static class FormatSerializationFilter {
    @Override
    public boolean equals(Object obj) {
      return obj == null || DEFAULT_FORMAT.equals(obj);
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
