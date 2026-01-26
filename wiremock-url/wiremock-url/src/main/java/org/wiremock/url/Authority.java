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

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wiremock.stringparser.ParsedString;

/**
 * Represents the authority component of a URI as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2">RFC 3986 Section 3.2</a>.
 *
 * <p>The authority component consists of optional user information, a host, and an optional port.
 * It typically takes the form {@code [userinfo@]host[:port]}.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2">RFC 3986 Section 3.2</a>
 */
public interface Authority extends Normalisable<Authority>, ParsedString {

  /**
   * Returns the user info component, or {@code null} if there is no user info.
   *
   * @return the user info, or {@code null} if absent
   */
  @Nullable UserInfo getUserInfo();

  /**
   * Returns the host component.
   *
   * @return the host, never {@code null}
   */
  Host getHost();

  /**
   * Returns the port component, or {@code null} if there is no port.
   *
   * @return the port, or {@code null} if absent
   */
  @Nullable Port getPort();

  /**
   * Returns an optional representation of the port to distinguish between no port and empty port.
   *
   * <p>An Authority can legitimately be any of:
   *
   * <ul>
   *   <li>{@code example.com} - no port (returns {@code null})
   *   <li>{@code example.com:} - empty port (returns {@code Optional.empty()})
   *   <li>{@code example.com:80} - with port (returns {@code Optional.of(port)})
   * </ul>
   *
   * @return {@code null} for no port, {@code Optional.empty()} for empty port, or {@code
   *     Optional.of(port)} for a port value
   */
  @Nullable Optional<Port> getMaybePort();

  /**
   * Returns the host and port as a {@link HostAndPort}.
   *
   * @return the host and port
   */
  HostAndPort getHostAndPort();

  /**
   * Returns a new authority with the specified port.
   *
   * @param port the port to set, or {@code null} to remove it
   * @return a new authority with the updated port
   */
  Authority withPort(@Nullable Port port);

  /**
   * Returns a new authority with the port removed.
   *
   * @return a new authority without a port
   */
  default Authority withoutPort() {
    return withPort(null);
  }

  /**
   * Returns a normalised form of this authority using scheme-specific normalization rules.
   *
   * <p>The scheme is used to determine if the port should be removed when it matches the default
   * port for that scheme.
   *
   * @param canonicalScheme the canonical scheme to use for normalization
   * @return a normalised authority
   */
  Authority normalise(Scheme canonicalScheme);

  @Override
  default boolean isNormalForm() {
    Optional<Port> maybePort = getMaybePort();
    //noinspection OptionalAssignedToNull
    return (getUserInfo() == null || getUserInfo().isNormalForm())
        && getHost().isNormalForm()
        && (maybePort == null || (maybePort.isPresent() && maybePort.get().isNormalForm()));
  }

  /**
   * Tests if this value is already normalised
   *
   * @param canonicalScheme the scheme to be in normal form against
   * @return true if in normal form for this scheme
   */
  default boolean isNormalForm(Scheme canonicalScheme) {
    return getHost().isNormalForm() && portIsNormalForm(canonicalScheme);
  }

  private boolean portIsNormalForm(Scheme scheme) {
    Optional<Port> maybePort = getMaybePort();
    //noinspection OptionalAssignedToNull
    return maybePort == null
        || (maybePort.isPresent()
            && maybePort.get().isNormalForm()
            && !maybePort.get().equals(scheme.getDefaultPort()));
  }

  /**
   * Parses a string into an authority.
   *
   * @param authorityStr the string to parse
   * @return the parsed authority
   * @throws IllegalAuthority if the string is not a valid authority
   */
  static Authority parse(String authorityStr) throws IllegalAuthority {
    return AuthorityParser.INSTANCE.parse(authorityStr);
  }

  /**
   * Creates an authority from a host.
   *
   * @param host the host
   * @return the authority
   */
  static Authority of(Host host) {
    return of(null, host, null);
  }

  /**
   * Creates an authority from a host and port.
   *
   * @param host the host
   * @param port the port, or {@code null}
   * @return the authority
   */
  static Authority of(Host host, @Nullable Port port) {
    return of(null, host, port);
  }

  /**
   * Creates an authority from user info and host.
   *
   * @param userInfo the user info, or {@code null}
   * @param host the host
   * @return the authority
   */
  static Authority of(@Nullable UserInfo userInfo, Host host) {
    return of(userInfo, host, null);
  }

  /**
   * Creates an authority from user info, host, and port.
   *
   * @param userInfo the user info, or {@code null}
   * @param host the host
   * @param port the port, or {@code null}
   * @return the authority
   */
  static Authority of(@Nullable UserInfo userInfo, Host host, @Nullable Port port) {
    return AuthorityParser.INSTANCE.of(userInfo, host, port);
  }

  static boolean equals(Authority one, Object o) {
    if (one == o) {
      return true;
    }

    if (!(o instanceof Authority other)) {
      return false;
    }

    return Objects.equals(one.getUserInfo(), other.getUserInfo())
        && Objects.equals(one.getHost(), other.getHost())
        && Objects.equals(one.getMaybePort(), other.getMaybePort());
  }

  static int hashCode(Authority authority) {
    return Objects.hash(authority.getUserInfo(), authority.getHost(), authority.getMaybePort());
  }
}
