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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.wiremock.url.Constants.multiplePctEncodedPattern;

import java.io.ByteArrayOutputStream;

/**
 * Represents a string that may contain percent-encoded characters as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1">RFC 3986 Section 2.1</a>.
 *
 * <p>Percent-encoding is a mechanism to represent characters that are not allowed or have special
 * meaning in URIs. Characters are encoded as a percent sign ({@code %}) followed by two hexadecimal
 * digits representing the byte value.
 *
 * <p>Implementations must provide a {@link #toString()} method that returns the percent-encoded
 * string representation.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1">RFC 3986 Section 2.1</a>
 */
public interface PercentEncoded<SELF extends PercentEncoded<SELF>> extends Normalisable<SELF> {

  /**
   * Decodes all percent-encoded sequences in this string.
   *
   * <p>Sequences like {@code %20} are decoded to their corresponding characters using UTF-8
   * encoding.
   *
   * @return the decoded string
   */
  default String decode() {
    return Strings.transform(
        toString(), multiplePctEncodedPattern, PercentEncoded::decodeCharacters);
  }

  /**
   * Returns the length of this percent-encoded string.
   *
   * @return the length of the string
   */
  default int length() {
    return toString().length();
  }

  /**
   * Returns {@code true} if this percent-encoded string is empty.
   *
   * @return {@code true} if the string has zero length
   */
  default boolean isEmpty() {
    return toString().isEmpty();
  }

  private static String decodeCharacters(String percentEncodings) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    for (int i = 0; i < percentEncodings.length(); ) {
      String hexString = percentEncodings.substring(i + 1, i + 3);
      int byteValue = Integer.parseInt(hexString, 16);
      bytes.write(byteValue);
      i += 3;
    }

    return bytes.toString(UTF_8);
  }
}
