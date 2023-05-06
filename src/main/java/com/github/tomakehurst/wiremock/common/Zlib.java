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
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Zlib implements CompressionAlgorithm {

  @Override
  public InputStream decompressionStream(InputStream source) {
    return new InflaterInputStream(source);
  }

  @Override
  public OutputStream compressionStream(OutputStream source) {
    return new DeflaterOutputStream(source);
  }

  @Override
  public boolean matches(byte[] content) {
    if (content.length < 2) return false;
    if (content[0] != (byte) 0x78) return false;
    return content[1] == (byte) 0x01
        || content[1] == (byte) 0x9C
        || content[1] == (byte) 0xDA
        || content[1] == (byte) 0x5E;
  }
}
