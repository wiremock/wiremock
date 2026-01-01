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
package org.wiremock.url;

import java.util.List;

/**
 * Represents the path component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3">RFC 3986 Section 3.3</a>.
 *
 * <p>A path consists of a sequence of segments separated by forward slashes. Paths can be absolute
 * (starting with {@code /}) or relative. Path segments may contain percent-encoded characters.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3">RFC 3986 Section 3.3</a>
 */
public interface Path extends PercentEncoded {

  /** An empty path with no segments. */
  Path EMPTY = PathParser.INSTANCE.parse("");

  /** A root path consisting of a single forward slash. */
  Path ROOT = PathParser.INSTANCE.parse("/");

  /**
   * Returns {@code true} if this path is absolute (starts with {@code /}).
   *
   * @return {@code true} if this is an absolute path
   */
  boolean isAbsolute();

  /**
   * Returns the segments of this path.
   *
   * @return the list of path segments, never {@code null}
   */
  List<Segment> getSegments();

  /**
   * Parses a string into a path.
   *
   * @param path the string to parse
   * @return the parsed path
   * @throws IllegalPath if the string is not a valid path
   */
  static Path parse(String path) throws IllegalPath {
    return PathParser.INSTANCE.parse(path);
  }

  /**
   * Encodes a string into a valid path with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded path
   */
  static Path encode(String unencoded) {
    return PathParser.INSTANCE.encode(unencoded);
  }

  /**
   * Returns a normalized form of this path with dot segments removed.
   *
   * @return a normalized path
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2">RFC 3986 Section
   *     6.2.2</a>
   */
  Path normalise();

  /**
   * Resolves the given path against this path using reference resolution rules.
   *
   * @param other the path to resolve
   * @return the resolved path
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5.2">RFC 3986 Section
   *     5.2</a>
   */
  Path resolve(Path other);

  /**
   * Returns {@code true} if this path is empty (has no segments).
   *
   * @return {@code true} if this path is empty
   */
  default boolean isEmpty() {
    return this.equals(Path.EMPTY);
  }
}
