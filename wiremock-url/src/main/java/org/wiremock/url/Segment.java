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
package org.wiremock.url;

/**
 * Represents a single segment of a path.
 *
 * <p>A path segment is a portion of a path between forward slashes. Special segments include the
 * empty segment, the dot segment ({@code .}), and the dot-dot segment ({@code ..}) used for
 * relative path resolution.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see Path
 */
public interface Segment extends PercentEncoded<Segment> {

  /** An empty path segment. */
  Segment EMPTY = new SegmentValue("");

  /** A dot segment ({@code .}) representing the current directory. */
  Segment DOT = new SegmentValue(".");

  /** A dot-dot segment ({@code ..}) representing the parent directory. */
  Segment DOT_DOT = new SegmentValue("..");

  /**
   * Parses a string into a path segment.
   *
   * @param segment the string to parse
   * @return the parsed segment
   * @throws IllegalSegment if the string is not a valid segment
   */
  static Segment parse(String segment) throws IllegalSegment {
    return SegmentParser.INSTANCE.parse(segment);
  }

  /**
   * Encodes a string into a valid path segment with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded segment
   */
  static Segment encode(String unencoded) {
    return SegmentParser.INSTANCE.encode(unencoded);
  }
}
