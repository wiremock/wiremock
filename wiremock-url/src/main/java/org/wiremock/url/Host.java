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
 * Represents the host component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">RFC 3986 Section 3.2.2</a>.
 *
 * <p>A host can be an IP address (IPv4 or IPv6), a registered name (domain name), or an IPvFuture
 * address. Registered names may contain percent-encoded characters.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">RFC 3986 Section
 *     3.2.2</a>
 */
public interface Host extends PercentEncoded<Host> {

  Host EMPTY = new HostValue("");

  /**
   * Returns a normalised form of this host.
   *
   * <p>Normalization includes converting registered names to lowercase and normalizing
   * percent-encoding.
   *
   * @return a normalised host
   */
  @Override
  Host normalise();

  /**
   * Parses a string into a host.
   *
   * @param hostString the string to parse
   * @return the parsed host
   * @throws IllegalHost if the string is not a valid host
   */
  static Host parse(String hostString) throws IllegalHost {
    return HostParser.INSTANCE.parse(hostString);
  }

  /**
   * Encodes a string into a valid host with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded host
   */
  static Host encode(String unencoded) {
    return HostParser.INSTANCE.encode(unencoded);
  }

  /**
   * Returns {@code true} if this host is in normal form.
   *
   * @return {@code true} if this is in normal form
   */
  boolean isNormalForm();
}
