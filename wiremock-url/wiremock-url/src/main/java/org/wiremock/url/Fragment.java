/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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

import org.wiremock.stringparser.ParsedString;

/**
 * Represents the fragment component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5">RFC 3986 Section 3.5</a>.
 *
 * <p>The fragment identifier provides direction to a secondary resource, such as a section within a
 * document. Fragments are not sent to the server but are used by the client. Fragment strings may
 * contain percent-encoded characters.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5">RFC 3986 Section 3.5</a>
 */
public interface Fragment extends PercentEncoded<Fragment>, ParsedString {

  Fragment EMPTY = new FragmentValue("", true);

  /**
   * Returns a normalised form of this fragment.
   *
   * @return a normalised fragment
   */
  @Override
  Fragment normalise();

  /**
   * Parses a string into a fragment.
   *
   * @param fragment the string to parse
   * @return the parsed fragment
   * @throws IllegalFragment if the string is not a valid fragment
   */
  static Fragment parse(String fragment) throws IllegalFragment {
    return FragmentParser.INSTANCE.parse(fragment);
  }

  /**
   * Encodes a string into a valid fragment with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded fragment
   */
  static Fragment encode(String unencoded) {
    return FragmentParser.INSTANCE.encode(unencoded);
  }
}
