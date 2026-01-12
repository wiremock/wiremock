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
 * Represents an authority component consisting of only a host and optional port, with no user info.
 *
 * <p>This is a specialized form of {@link Authority} that is guaranteed to have no user
 * information. It is commonly used for origins and server identification.
 *
 * <p>Implementations must be immutable and thread-safe.
 */
public interface HostAndPort extends Authority {

  HostAndPort EMPTY = new HostAndPortValue(Host.EMPTY, null);

  /**
   * Creates a host and port from a host.
   *
   * @param host the host
   * @return the host and port
   */
  static HostAndPort of(Host host) {
    return of(host, null);
  }

  /**
   * Creates a host and port from a host and optional port.
   *
   * @param host the host
   * @param port the port, or {@code null}
   * @return the host and port
   */
  static HostAndPort of(Host host, @Nullable Port port) {
    if (host.isEmpty()) {
      return HostAndPort.EMPTY;
    } else {
      return new HostAndPortValue(host, port);
    }
  }

  /**
   * Parses a string into a host and port.
   *
   * @param hostAndPortStr the string to parse
   * @return the parsed host and port
   * @throws IllegalHostAndPort if the string is not a valid host and port
   */
  static HostAndPort parse(String hostAndPortStr) {
    return HostAndPortParser.INSTANCE.parse(hostAndPortStr);
  }

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated // no point ever calling on this subtype
  default UserInfo getUserInfo() {
    return null;
  }

  /**
   * {@implSpec} Implementations must ALWAYS return this
   *
   * @deprecated This always returns this so you have no reason to ever call it
   * @return this
   */
  @Override
  @Deprecated // no point ever calling on this subtype
  default HostAndPort getHostAndPort() {
    return this;
  }

  /**
   * Returns a new host and port with the specified port.
   *
   * @param port the port to set, or {@code null} to remove it
   * @return a new host and port with the updated port
   */
  @Override
  HostAndPort withPort(@Nullable Port port);

  /**
   * Returns a normalized form of this host and port.
   *
   * @return a normalized host and port
   */
  @Override
  HostAndPort normalise();

  /**
   * Returns a normalized form of this host and port using scheme-specific normalization rules.
   *
   * @param canonicalScheme the canonical scheme to use for normalization
   * @return a normalized host and port
   */
  HostAndPort normalise(Scheme canonicalScheme);
}
