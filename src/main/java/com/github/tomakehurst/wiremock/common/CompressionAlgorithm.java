/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.Strings.DEFAULT_CHARSET;
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;

import com.google.common.io.ByteStreams;
import java.io.*;
import java.nio.charset.Charset;

public interface CompressionAlgorithm {

  /**
   * Convert a stream of compressed bytes to decompressed, readable bytes.
   *
   * @param source A stream of bytes previously compressed with this compression algorithm.
   * @return A stream that can be used to convert compressed bytes to decompressed, readable bytes.
   */
  InputStream decompressionStream(InputStream source);

  /**
   * Compress bytes before passing them along to an OutputStream.
   *
   * @param outputStream A stream of bytes connected to some output destination.
   * @return A stream that will compress bytes prior to passing them to outputStream.
   */
  OutputStream compressionStream(OutputStream outputStream);

  /**
   * Detect if bytes, based on their header, match this algorithm. May return false if insufficient
   * bytes are available.
   *
   * @param bytes The bytes to test.
   * @return true if these bytes have been compressed with this compression algorithm.
   */
  boolean matches(byte[] bytes);

  default byte[] decompress(byte[] compressedContent) {
    if (compressedContent.length == 0) {
      return new byte[0];
    }
    try {
      InputStream stream = decompressionStream(new ByteArrayInputStream(compressedContent));
      return ByteStreams.toByteArray(stream);
    } catch (IOException e) {
      return throwUnchecked(e, byte[].class);
    }
  }

  default String decompressToString(byte[] gzippedContent) {
    return new String(decompress(gzippedContent));
  }

  default byte[] compress(byte[] plainContent) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      OutputStream stream = compressionStream(bytes);
      stream.write(plainContent);
      stream.close();
      return bytes.toByteArray();
    } catch (IOException e) {
      return throwUnchecked(e, byte[].class);
    }
  }

  default byte[] compress(String plainContent, Charset charset) {
    return compress(bytesFromString(plainContent, charset));
  }

  default byte[] compress(String plainContent) {
    return compress(plainContent, DEFAULT_CHARSET);
  }
}
