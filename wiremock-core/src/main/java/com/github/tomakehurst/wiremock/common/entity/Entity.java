/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition.DEFAULT_COMPRESSION;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Limit;
import com.github.tomakehurst.wiremock.common.StreamSources;
import com.github.tomakehurst.wiremock.common.Strings;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;

public class Entity {

  public static Entity EMPTY =
      new Entity(
          EncodingType.TEXT, FormatType.TEXT, UTF_8, CompressionType.NONE, StreamSources.empty());

  private final EncodingType encoding;
  private final FormatType format;
  private final Charset charset;
  @NonNull private final CompressionType compression;
  private final InputStreamSource streamSource;

  public Entity(
      EncodingType encoding,
      FormatType format,
      Charset charset,
      CompressionType compression,
      InputStreamSource streamSource) {
    this.encoding = encoding;
    this.format = format;
    this.charset = charset;
    this.compression = getFirstNonNull(compression, DEFAULT_COMPRESSION);
    this.streamSource = streamSource;
  }

  public EncodingType getEncoding() {
    return encoding;
  }

  public FormatType getFormat() {
    return format;
  }

  public Charset getCharset() {
    return charset;
  }

  public CompressionType getCompression() {
    return compression;
  }

  public boolean isCompressed() {
    return compression != null && compression != NONE;
  }

  public boolean isDecompressible() {
    return compression == NONE || compression == GZIP;
  }

  public Entity decompress() {
    if (compression == GZIP) {
      return transform(
          builder ->
              builder
                  .setStreamSource(StreamSources.decompressingGzip(streamSource))
                  .setCompression(NONE));
    }

    if (compression != NONE) {
      throw new IllegalStateException("Cannot decompress body with compression " + compression);
    }

    return this;
  }

  public byte[] getData() {
    return getData(UNLIMITED);
  }

  public byte[] getData(Limit sizeLimit) {
    return Exceptions.uncheck(() -> getBytesFromStream(streamSource, sizeLimit), byte[].class);
  }

  private static byte[] getBytesFromStream(InputStreamSource streamSource, Limit limit) {
    if (streamSource == null) {
      return null;
    }

    return Exceptions.uncheck(
        () -> {
          try (InputStream stream =
              Exceptions.uncheck(streamSource::getStream, InputStream.class)) {
            if (stream == null) {
              return null;
            }

            return limit != null && !limit.isUnlimited()
                ? stream.readNBytes(limit.getValue())
                : stream.readAllBytes();
          }
        },
        byte[].class);
  }

  public InputStreamSource getStreamSource() {
    return streamSource;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Entity transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Entity transformUncompressedDataString(Function<String, String> transformer) {
    if (isDecompressible()) {
      return transform(
          builder -> {
            final String plainText =
                compression == GZIP
                    ? Gzip.unGzipToString(getData())
                    : Strings.stringFromBytes(getData());

            final String transformed = transformer.apply(plainText);

            final byte[] transformedCompressed =
                compression == GZIP
                    ? Gzip.gzip(transformed)
                    : Strings.bytesFromString(transformed, UTF_8);

            builder.setBody(transformedCompressed);
          });
    }

    throw new IllegalStateException(
        "Cannot decompress body with compression " + compression.value());
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Entity entity = (Entity) o;
    return Objects.equals(encoding, entity.encoding)
        && Objects.equals(format, entity.format)
        && Objects.equals(compression, entity.compression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(encoding, format, compression);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Entity.class.getSimpleName() + "[", "]")
        .add("encoding=" + encoding)
        .add("format=" + format)
        .add("compression=" + compression)
        .add("streamSource=" + streamSource)
        .toString();
  }

  public static class Builder {

    private EncodingType encoding;
    private FormatType format;
    private Charset charset;
    private CompressionType compression;
    private InputStreamSource streamSource;

    public Builder() {}

    public Builder(Entity entity) {
      this.encoding = entity.encoding;
      this.format = entity.format;
      this.compression = entity.compression;
      this.streamSource = entity.streamSource;
    }

    public EncodingType getEncoding() {
      return encoding;
    }

    public Builder setEncoding(EncodingType encoding) {
      this.encoding = encoding;
      return this;
    }

    public FormatType getFormat() {
      return format;
    }

    public Builder setFormat(FormatType format) {
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

    public CompressionType getCompression() {
      return compression;
    }

    public Builder setCompression(CompressionType compression) {
      this.compression = compression;
      return this;
    }

    public InputStreamSource getStreamSource() {
      return streamSource;
    }

    public Builder setStreamSource(InputStreamSource streamSource) {
      this.streamSource = streamSource;
      return this;
    }

    public Builder setBody(byte[] bytes) {
      this.streamSource = StreamSources.forBytes(bytes);
      return this;
    }

    public Builder setBody(String text) {
      return setBody(text, UTF_8);
    }

    public Builder setBody(String text, Charset charset) {
      this.streamSource = StreamSources.forString(text, charset);
      return this;
    }

    public String getDataAsString() {
      return Exceptions.uncheck(() -> Strings.stringFromBytes(getData(), UTF_8), String.class);
    }

    public byte[] getData() {
      return Exceptions.uncheck(() -> getBytesFromStream(streamSource, UNLIMITED), byte[].class);
    }

    public boolean isDecompressible() {
      return compression == NONE || compression == GZIP;
    }

    public Entity build() {
      return new Entity(encoding, format, charset, compression, streamSource);
    }
  }
}
