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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
final class HostAndPortValue implements HostAndPort {

  private final Host host;
  private final @Nullable Port port;

  HostAndPortValue(Host host, @Nullable Port port) {
    this.host = requireNonNull(host);
    this.port = port;
  }

  @Override
  public String toString() {
    if (port != null) {
      return host + ":" + port;
    } else {
      return host.toString();
    }
  }

  @Override
  public @Nullable Optional<Port> getMaybePort() {
    //noinspection OptionalAssignedToNull
    return port != null ? Optional.of(port) : null;
  }

  @Override
  public HostAndPort withPort(@Nullable Port port) {
    if (Objects.equals(port, this.port)) {
      return this;
    } else {
      return HostAndPort.of(host, port);
    }
  }

  @Override
  public HostAndPort normalise() {
    var normalisedHost = host.normalise();
    var normalisedPort = port == null ? null : port.normalise();
    return normalised(normalisedHost, normalisedPort);
  }

  @Override
  public HostAndPort normalise(Scheme canonicalScheme) {
    var normalisedHost = host.normalise();
    var normalisedPort = port == null ? null : port.normalise();
    if (Objects.equals(canonicalScheme.getDefaultPort(), normalisedPort)) {
      normalisedPort = null;
    }
    return normalised(normalisedHost, normalisedPort);
  }

  private HostAndPort normalised(Host normalisedHost, @Nullable Port normalisedPort) {
    return normalisedHost.equals(host) && Objects.equals(normalisedPort, port)
        ? this
        : HostAndPort.of(normalisedHost, normalisedPort);
  }

  @Override
  public Host getHost() {
    return host;
  }

  @Override
  public @Nullable Port getPort() {
    return port;
  }

  @Override
  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public boolean equals(Object other) {
    return Authority.equals(this, other);
  }

  @Override
  public int hashCode() {
    return Authority.hashCode(this);
  }
}
