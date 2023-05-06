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

import java.io.InputStream;
import java.io.OutputStream;

public enum Compression implements CompressionAlgorithm {
  GZIP("gzip", new Gzip()),
  DEFLATE("deflate", new Zlib());

  public final String contentEncodingValue;
  public final CompressionAlgorithm algorithm;

  Compression(String contentEncodingValue, CompressionAlgorithm algorithm) {
    this.contentEncodingValue = contentEncodingValue;
    this.algorithm = algorithm;
  }

  @Override
  public InputStream decompressionStream(InputStream source) {
    return algorithm.decompressionStream(source);
  }

  @Override
  public OutputStream compressionStream(OutputStream source) {
    return algorithm.compressionStream(source);
  }

  @Override
  public boolean matches(byte[] bytes) {
    return algorithm.matches(bytes);
  }
}
