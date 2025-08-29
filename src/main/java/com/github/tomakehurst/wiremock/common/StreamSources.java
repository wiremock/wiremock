/*
 * Copyright (C) 2018-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.store.BlobStore;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/** The type Stream sources. */
public class StreamSources {
  private StreamSources() {}

  /**
   * For string input stream source.
   *
   * @param string the string
   * @param charset the charset
   * @return the input stream source
   */
  public static InputStreamSource forString(final String string, final Charset charset) {
    return new StringInputStreamSource(string, charset);
  }

  /**
   * For bytes input stream source.
   *
   * @param bytes the bytes
   * @return the input stream source
   */
  public static InputStreamSource forBytes(final byte[] bytes) {
    return new ByteArrayInputStreamSource(bytes);
  }

  /**
   * For blob store item input stream source.
   *
   * @param blobStore the blob store
   * @param key the key
   * @return the input stream source
   */
  public static InputStreamSource forBlobStoreItem(BlobStore blobStore, String key) {
    return () ->
        blobStore
            .getStream(key)
            .orElseThrow(() -> new NotFoundException("Not found in blob store: " + key));
  }

  /** The type String input stream source. */
  public static class StringInputStreamSource extends ByteArrayInputStreamSource {

    /**
     * Instantiates a new String input stream source.
     *
     * @param string the string
     * @param charset the charset
     */
    public StringInputStreamSource(String string, Charset charset) {
      super(Strings.bytesFromString(string, charset));
    }
  }

  /** The type Byte array input stream source. */
  public static class ByteArrayInputStreamSource implements InputStreamSource {

    private final byte[] bytes;

    /**
     * Instantiates a new Byte array input stream source.
     *
     * @param bytes the bytes
     */
    public ByteArrayInputStreamSource(byte[] bytes) {
      this.bytes = bytes;
    }

    @Override
    public InputStream getStream() {
      return bytes == null ? null : new ByteArrayInputStream(bytes);
    }
  }

  /**
   * Empty input stream source.
   *
   * @return the input stream source
   */
  public static InputStreamSource empty() {
    return forBytes(new byte[0]);
  }
}
