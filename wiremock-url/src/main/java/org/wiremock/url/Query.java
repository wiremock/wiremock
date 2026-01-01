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

/**
 * Represents the query component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4">RFC 3986 Section 3.4</a>.
 *
 * <p>The query component contains non-hierarchical data, typically formatted as key-value pairs
 * separated by ampersands. Query strings may contain percent-encoded characters.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4">RFC 3986 Section 3.4</a>
 */
public interface Query extends PercentEncoded {

  /**
   * Parses a string into a query.
   *
   * @param query the string to parse
   * @return the parsed query
   * @throws IllegalQuery if the string is not a valid query
   */
  static Query parse(CharSequence query) throws IllegalQuery {
    return QueryParser.INSTANCE.parse(query);
  }

  /**
   * Encodes a string into a valid query with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded query
   */
  static Query encode(String unencoded) {
    return QueryParser.INSTANCE.encode(unencoded);
  }

  /**
   * Returns a normalized form of this query using default HTTP scheme normalization rules.
   *
   * @return a normalized query
   */
  default Query normalise() {
    return normalise(Scheme.http);
  }

  /**
   * Returns a normalized form of this query using scheme-specific normalization rules.
   *
   * @param scheme the scheme to use for normalization
   * @return a normalized query
   */
  Query normalise(Scheme scheme);
}
