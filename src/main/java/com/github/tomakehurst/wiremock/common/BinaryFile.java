/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
import java.net.URI;

/** The type Binary file. */
public class BinaryFile implements InputStreamSource {

  private final URI uri;

  /**
   * Instantiates a new Binary file.
   *
   * @param uri the uri
   */
  public BinaryFile(URI uri) {
    this.uri = uri;
  }

  /**
   * Read contents byte [ ].
   *
   * @return the byte [ ]
   */
  public byte[] readContents() {
    try (InputStream stream = getStream()) {
      return stream.readAllBytes();
    } catch (final IOException ioe) {
      return throwUnchecked(ioe, byte[].class);
    }
  }

  /**
   * Gets uri.
   *
   * @return the uri
   */
  protected URI getUri() {
    return uri;
  }

  /**
   * Name string.
   *
   * @return the string
   */
  public String name() {
    return uri.toString();
  }

  @Override
  public String toString() {
    return name();
  }

  @Override
  public InputStream getStream() {
    try {
      return uri.toURL().openStream();
    } catch (IOException e) {
      return throwUnchecked(e, InputStream.class);
    }
  }
}
