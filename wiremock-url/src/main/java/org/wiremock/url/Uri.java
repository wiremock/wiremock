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
 * Represents a URI reference as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>. The name {@link Uri} is used
 * in preference to UriReference to maintain consistency with the names {@link Url}, {@link
 * AbsoluteUrl} and {@link RelativeUrl} elsewhere in the hierarchy, which are chosen for familiarity
 * to developers.
 *
 * <p>An {@link Uri} is either an {@link AbsoluteUri} or an {@link Url}. {@link Uri}s are used to
 * identify resources and can be resolved against an {@link AbsoluteUri} to produce an {@link
 * AbsoluteUri}.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.1">RFC 3986 Section 4.1</a>
 */
public sealed interface Uri permits AbsoluteUri, AbstractUriValue, Url {

  /**
   * Returns the scheme component of this URI, or {@code null} if it is a URI Reference and so there
   * is no scheme.
   *
   * @return the scheme component, or {@code null} if absent
   */
  @Nullable Scheme getScheme();

  /**
   * Returns the authority component of this URI reference, or {@code null} if there is no
   * authority.
   *
   * @return the authority component, or {@code null} if absent
   */
  @Nullable Authority getAuthority();

  /**
   * Returns the path component of this URI reference.
   *
   * @return the path component, never {@code null}
   */
  Path getPath();

  /**
   * Returns the query component of this URI reference, or {@code null} if there is no query.
   *
   * @return the query component, or {@code null} if absent
   */
  @Nullable Query getQuery();

  /**
   * Returns the fragment component of this URI reference, or {@code null} if there is no fragment.
   *
   * @return the fragment component, or {@code null} if absent
   */
  @Nullable Fragment getFragment();

  /**
   * Returns {@code true} if this is a relative reference (has no scheme).
   *
   * @return {@code true} if this is a relative reference
   */
  boolean isRelative();

  /**
   * Returns {@code true} if this is an absolute URI (either an absolute URL or an Opaque URI).
   *
   * @return {@code true} if this is an absolute URI
   */
  boolean isAbsolute();

  /**
   * Returns {@code true} if this is an absolute URL (has a scheme and authority).
   *
   * @return {@code true} if this is an absolute URL
   */
  boolean isAbsoluteUrl();

  /**
   * Returns {@code true} if this is an Opaque URI (has a scheme but no authority).
   *
   * @return {@code true} if this is an Opaque URI
   */
  boolean isOpaqueUri();

  /**
   * Returns the user info component from the authority, or {@code null} if there is no authority or
   * no user info.
   *
   * @return the user info component, or {@code null} if absent
   */
  default @Nullable UserInfo getUserInfo() {
    Authority authority = getAuthority();
    return authority != null ? authority.getUserInfo() : null;
  }

  /**
   * Returns the host component from the authority, or {@code null} if there is no authority.
   *
   * @return the host component, or {@code null} if absent
   */
  default @Nullable Host getHost() {
    Authority authority = getAuthority();
    return authority != null ? authority.getHost() : null;
  }

  /**
   * Returns the port component from the authority, or {@code null} if there is no authority or no
   * port.
   *
   * @return the port component, or {@code null} if absent
   */
  default @Nullable Port getPort() {
    Authority authority = getAuthority();
    return authority != null ? authority.getPort() : null;
  }

  /**
   * Returns the resolved port, which is either the explicitly defined port or the default port for
   * the scheme.
   *
   * @return the resolved port, or {@code null} if no port is defined and no default exists for the
   *     scheme
   */
  default @Nullable Port getResolvedPort() {
    Port definedPort = getPort();
    Scheme scheme = getScheme();
    return definedPort != null ? definedPort : (scheme != null ? scheme.getDefaultPort() : null);
  }

  /**
   * Parses a string into a URI reference.
   *
   * @param uri the string to parse
   * @return the parsed URI reference
   * @throws IllegalUri if the string is not a valid URI reference
   */
  static Uri parse(String uri) throws IllegalUri {
    return UriParser.INSTANCE.parse(uri);
  }

  /**
   * Creates a new builder for constructing URI references.
   *
   * @return a new builder
   */
  static Uri.Builder builder() {
    return new UriBuilder();
  }

  /**
   * Creates a new builder initialized with the values from the given URI reference.
   *
   * @param uri the URI reference to copy values from
   * @return a new builder
   */
  static Uri.Builder builder(Uri uri) {
    return new UriBuilder(uri);
  }

  interface Builder extends UriBaseBuilder<Builder> {
    Uri.Builder setScheme(@Nullable Scheme scheme);

    @Override
    Uri.Builder setAuthority(@Nullable Authority authority);
  }

  interface Transformer<SELF extends Transformer<SELF>> extends UriBaseBuilder<SELF> {
    SELF setScheme(Scheme scheme);
  }
}
