/*
 * Copyright (C) 2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.Limit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class Entity {

  private final EncodingType encoding;
  private final FormatType format;
  private final CompressionType compression;
  private final InputStreamSource streamSource;

  public Entity(
      EncodingType encoding,
      FormatType format,
      CompressionType compression,
      InputStreamSource streamSource) {
    this.encoding = encoding;
    this.format = format;
    this.compression = compression;
    this.streamSource = streamSource;
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

  public byte[] getData() {
    return getData(UNLIMITED);
  }

  public byte[] getData(Limit sizeLimit) {
    return Exceptions.uncheck(() -> getBytesFromStream(streamSource, sizeLimit), byte[].class);
  }

  private static byte[] getBytesFromStream(InputStreamSource streamSource, Limit limit)
      throws IOException {
    try (InputStream stream = streamSource == null ? null : streamSource.getStream()) {
      if (stream == null) {
        return null;
      }

      return limit != null && !limit.isUnlimited()
          ? stream.readNBytes(limit.getValue())
          : stream.readAllBytes();
    }
  }

  public InputStreamSource getStreamSource() {
    return streamSource;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Entity entity = (Entity) o;
    return Objects.equals(encoding, entity.encoding)
        && Objects.equals(format, entity.format)
        && Objects.equals(compression, entity.compression)
        && Arrays.equals(getData(), entity.getData());
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
}
