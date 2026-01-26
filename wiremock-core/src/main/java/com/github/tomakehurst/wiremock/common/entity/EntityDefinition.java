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
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

@JsonInclude(NON_NULL)
@JsonDeserialize(using = EntityDefinitionDeserializer.class)
@JsonSubTypes(
    value = {
      @JsonSubTypes.Type(TextEntityDefinition.class),
      @JsonSubTypes.Type(SimpleStringEntityDefinition.class),
      @JsonSubTypes.Type(JsonEntityDefinition.class),
      @JsonSubTypes.Type(BinaryEntityDefinition.class)
    })
public abstract class EntityDefinition<SELF extends EntityDefinition<SELF>> {

  public static final Charset DEFAULT_CHARSET = UTF_8;
  public static final CompressionType DEFAULT_COMPRESSION = CompressionType.NONE;

  @NonNull protected final CompressionType compression;
  protected final TextFormat textFormat;

  protected EntityDefinition(@NonNull CompressionType compression, TextFormat textFormat) {
    this.compression = getFirstNonNull(compression, DEFAULT_COMPRESSION);
      this.textFormat = textFormat;
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

  @NonNull public CompressionType getCompression() {
    return compression;
  }

  public TextFormat getFormat() {
    return textFormat;
  }

  @NonNull public abstract EncodingType getEncoding();


  public abstract DataFormat getDataFormat();
  public abstract Object getData();

  @JsonIgnore
  public boolean isInline() {
    return getData() != null;
  }

  @JsonIgnore
  public boolean isFromFile() {
    return getFilePath() != null;
  }

  @JsonIgnore
  public abstract String getDataAsString();

  @JsonIgnore
  public abstract byte[] getDataAsBytes();

  @JsonIgnore
  public boolean isDecompressable() {
    final CompressionType compression = getCompression();
    return compression == NONE || compression == GZIP;
  }

  public String getFilePath() {
    if (FILES_ROOT.equals(getDataStore()) && getDataRef() != null) {
      return getDataRef();
    }

    return null;
  }

  public abstract String getDataStore();

  public abstract String getDataRef();

  public EntityDefinition<?> transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  @SuppressWarnings("unchecked")
  public SELF decompress() {
    final CompressionType compression = getCompression();
    if (compression == GZIP) {
      return (SELF)
          transform(builder -> builder
                  .setData(Gzip.unGzip(getDataAsBytes()))
                  .setCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return (SELF) this;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private EncodingType encoding = EncodingType.TEXT;
    private CompressionType compression = NONE;

    protected TextFormat format;
    protected Charset charset = DEFAULT_CHARSET;

    private byte[] data;
    private Object jsonData;

    private String dataStore;
    private String dataRef;
    private String filePath;

    private boolean v3Style = false;

    public Builder() {}

    public Builder(EntityDefinition<?> entity) {
      this.encoding = entity.getEncoding();
      this.compression = entity.getCompression();

      if (entity instanceof TextEntityDefinition textEntity) {
        this.format = textEntity.getFormat();
        this.charset = textEntity.getCharset();
      }

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

    public EncodingType getEncoding() {
      return encoding;
    }

    public Builder setEncoding(EncodingType encoding) {
      this.encoding = encoding;
      return this;
    }

    public CompressionType getCompression() {
      return compression;
    }

    public Builder setCompression(CompressionType compression) {
      this.compression = compression;
      return this;
    }

    public TextFormat getFormat() {
      return format;
    }

    public Builder setFormat(TextFormat format) {
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

    public Builder setData(Object data) {
      resetDataAndRefs();
      this.data =
          data instanceof String s
              ? Strings.bytesFromString(s, charset)
              : data instanceof byte[] b ? b : null;
      return this;
    }

    public Builder setBodyBase64(String base64Data) {
      resetDataAndRefs();
      this.data = Encoding.decodeBase64(base64Data);
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

    public EntityDefinition<?> build() {
      if (jsonData != null) {
        return new JsonEntityDefinition(jsonData);
      }

      if (encoding == EncodingType.TEXT) {
        var dataFormat = compression == NONE ? DataFormat.plain : DataFormat.base64;
        var formattedData = dataFormat == DataFormat.base64 ? Encoding.encodeBase64(data) : data;
        return v3Style
            ? new SimpleStringEntityDefinition(Strings.stringFromBytes(data, charset))
            : new TextEntityDefinition(
                format, charset, compression, dataStore, dataRef, dataFormat, formattedData, filePath);
      } else {
        return new BinaryEntityDefinition(null, format, compression, dataStore, dataRef, data, filePath);
      }
    }
  }
}
