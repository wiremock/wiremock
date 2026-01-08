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

import org.jspecify.annotations.Nullable;

/**
 * Represents a web origin as defined in <a
 * href="https://html.spec.whatwg.org/multipage/origin.html#concept-origin">HTML Living
 * Standard</a>.
 *
 * <p>An origin consists of a scheme, host, and port. It represents the security context of a web
 * resource and is used for same-origin policy enforcement. Origins have no path, query, or fragment
 * components.
 *
 * <p>The component parts of an origin (scheme, host and port) are always normalised. The origin
 * itself is <b>not</b> the normal form of the URL it represents, since it has an empty path rather
 * than an absolute root path. {@code Origin.parse("https://example.org").normalise().toString() }
 * will return {@code https://example.org/ }.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/origin.html#concept-origin">HTML Living
 *     Standard - Origin</a>
 */
public interface Origin extends AbsoluteUrl {

  /**
   * Returns the authority component of this origin.
   *
   * <p>Origins always have an authority that is a {@link HostAndPort} (no user info).
   *
   * @return the authority component, never {@code null}
   */
  @Override
  HostAndPort getAuthority();

  /**
   * {@implSpec} Implementations must ALWAYS return {@link PathAndQuery#EMPTY}
   *
   * @deprecated This always returns empty so you have no reason to ever call it
   * @return {@link PathAndQuery#EMPTY}
   */
  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default PathAndQuery getPathAndQuery() {
    return PathAndQuery.EMPTY;
  }

  /**
   * {@implSpec} Implementations must ALWAYS return {@link Path#EMPTY}
   *
   * @deprecated This always returns empty so you have no reason to ever call it
   * @return {@link Path#EMPTY}
   */
  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default Path getPath() {
    return Path.EMPTY;
  }

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  @Nullable
  default Query getQuery() {
    return null;
  }

  /**
   * Returns a normalised form of this origin by setting the path to {@code / }.
   *
   * @return a normalised origin (as a URL)
   */
  @Override
  AbsoluteUrl normalise();

  /**
   * Creates an origin from a scheme and host/port.
   *
   * @param scheme the scheme
   * @param hostAndPort the host and port
   * @return the origin
   * @throws IllegalOrigin if any of the scheme, host and port are not normalised
   */
  static Origin of(Scheme scheme, HostAndPort hostAndPort) throws IllegalOrigin {
    return OriginParser.INSTANCE.of(scheme, hostAndPort);
  }

  /**
   * Parses a string into an origin.
   *
   * @param origin the string to parse
   * @return the parsed origin
   * @throws IllegalOrigin if the string is not a valid origin
   */
  static Origin parse(String origin) throws IllegalOrigin {
    return OriginParser.INSTANCE.parse(origin);
  }
}
