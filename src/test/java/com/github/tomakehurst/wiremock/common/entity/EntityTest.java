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

import static com.github.tomakehurst.wiremock.common.entity.CompressionType.BROTLI;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.DEFLATE;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.GZIP;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Strings;
import org.junit.jupiter.api.Test;

class EntityTest {

  private static final String TEST_DATA = "Initial data";

  @Test
  void transformsGzippedData() {
    byte[] gzippedData = Gzip.gzip(TEST_DATA);

    Entity initial = Entity.builder().setCompression(GZIP).setData(gzippedData).build();

    Entity transformed =
        initial.transformUncompressedDataString(plain -> plain.replace("Initial", "Modified"));
    assertThat(transformed.isCompressed(), is(true));
    assertThat(transformed.getCompression(), is(GZIP));

    Entity decompressed = transformed.decompress();
    assertThat(decompressed.getCompression(), is(NONE));
    assertThat(Strings.stringFromBytes(decompressed.getData()), is("Modified data"));
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressBrotli() {
    Entity entity =
        Entity.builder().setFormat(Format.TEXT).setCompression(BROTLI).setData(TEST_DATA).build();

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> entity.transformUncompressedDataString(source -> source));

    assertThat(exception.getMessage(), equalTo("Cannot decompress body with compression brotli"));
  }

  @Test
  void throwsExceptionWhenAttemptingToDecompressDeflate() {
    Entity entity =
        Entity.builder().setFormat(Format.TEXT).setCompression(DEFLATE).setData(TEST_DATA).build();

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> entity.transformUncompressedDataString(source -> source));

    assertThat(exception.getMessage(), equalTo("Cannot decompress body with compression deflate"));
  }

  @Test
  void isCompressedReturnsTrueForGzipCompression() {
    Entity gzippedEntity =
        Entity.builder().setFormat(Format.TEXT).setCompression(GZIP).setData(TEST_DATA).build();

    assertThat(gzippedEntity.isCompressed(), is(true));
  }

  @Test
  void isCompressedReturnsTrueForBrotliCompression() {
    Entity brotliEntity =
        Entity.builder().setFormat(Format.TEXT).setCompression(BROTLI).setData(TEST_DATA).build();

    assertThat(brotliEntity.isCompressed(), is(true));
  }

  @Test
  void isCompressedReturnsTrueForDeflateCompression() {
    Entity deflateEntity =
        Entity.builder().setFormat(Format.TEXT).setCompression(DEFLATE).setData(TEST_DATA).build();

    assertThat(deflateEntity.isCompressed(), is(true));
  }

  @Test
  void isCompressedReturnsFalseForNoCompression() {
    Entity uncompressedEntity =
        Entity.builder().setFormat(Format.TEXT).setCompression(NONE).setData(TEST_DATA).build();

    assertThat(uncompressedEntity.isCompressed(), is(false));
  }

  @Test
  void isDecompressibleReturnsTrueForGzipCompression() {
    Entity.Builder builder =
        Entity.builder().setFormat(Format.TEXT).setCompression(GZIP).setData(TEST_DATA);

    assertThat(builder.isDecompressible(), is(true));
  }

  @Test
  void isDecompressibleReturnsTrueForNoCompression() {
    Entity.Builder builder =
        Entity.builder().setFormat(Format.TEXT).setCompression(NONE).setData(TEST_DATA);

    assertThat(builder.isDecompressible(), is(true));
  }

  @Test
  void isDecompressibleReturnsFalseForBrotliCompression() {
    Entity.Builder builder =
        Entity.builder().setFormat(Format.TEXT).setCompression(BROTLI).setData(TEST_DATA);

    assertThat(builder.isDecompressible(), is(false));
  }

  @Test
  void isDecompressibleReturnsFalseForDeflateCompression() {
    Entity.Builder builder =
        Entity.builder().setFormat(Format.TEXT).setCompression(DEFLATE).setData(TEST_DATA);

    assertThat(builder.isDecompressible(), is(false));
  }
}
