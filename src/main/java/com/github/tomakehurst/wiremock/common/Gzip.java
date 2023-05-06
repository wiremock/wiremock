/*
 * Copyright (C) 2015-2023 Thomas Akehurst
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip implements CompressionAlgorithm {

  @Override
  public InputStream decompressionStream(InputStream source) {
    try {
      return new GZIPInputStream(source);
    } catch (IOException e) {
      return throwUnchecked(e, InputStream.class);
    }
  }

  @Override
  public OutputStream compressionStream(OutputStream outputStream) {
    try {
      return new GZIPOutputStream(outputStream);
    } catch (IOException e) {
      return throwUnchecked(e, OutputStream.class);
    }
  }

  @Override
  public boolean matches(byte[] content) {
    return content.length >= 2
        && content[0] == (byte) GZIPInputStream.GZIP_MAGIC
        && content[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
  }
}
