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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ClassCanBeRecord"})
final class AuthorityValue implements Authority {

  private final @Nullable UserInfo userInfo;
  private final Host host;
  private final @Nullable Optional<Port> maybePort;

  AuthorityValue(@Nullable UserInfo userInfo, Host host, @Nullable Optional<Port> maybePort) {
    this.userInfo = userInfo;
    this.host = requireNonNull(host);
    this.maybePort = maybePort;
  }

  @Override
  public @Nullable Port getPort() {
    //noinspection OptionalAssignedToNull
    return maybePort != null ? maybePort.orElse(null) : null;
  }

  @Override
  @Nullable
  public Optional<Port> getMaybePort() {
    return maybePort;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (userInfo != null) {
      result.append(userInfo).append('@');
    }
    result.append(host);

    //noinspection OptionalAssignedToNull
    if (maybePort != null) {
      result.append(':');
      maybePort.ifPresent(result::append);
    }
    return result.toString();
  }

  @Override
  public HostAndPort getHostAndPort() {
    return HostAndPort.of(host, getPort());
  }

  @Override
  public Authority withPort(@Nullable Port port) {
    @SuppressWarnings("OptionalAssignedToNull")
    Optional<Port> newPort = port != null ? Optional.of(port) : null;
    if (Objects.equals(newPort, maybePort)) {
      return this;
    } else {
      return new AuthorityValue(userInfo, host, newPort);
    }
  }

  @Override
  public Authority normalise() {
    var normalisedPort = normalisePort();
    return buildNormalisedAuthority(normalisedPort);
  }

  @Override
  public Authority normalise(Scheme canonicalScheme) {
    var normalisedPort = normalisePort(canonicalScheme);
    return buildNormalisedAuthority(normalisedPort);
  }

  private @Nullable Port normalisePort(Scheme canonicalScheme) {
    var normalisedPort = normalisePort();
    return Objects.equals(normalisedPort, canonicalScheme.getDefaultPort()) ? null : normalisedPort;
  }

  private @Nullable Port normalisePort() {
    var port = getPort();
    return port != null ? port.normalise() : null;
  }

  private Authority buildNormalisedAuthority(@Nullable Port normalisedPort) {
    var normalisedUserInfo = userInfo != null ? userInfo.normalise() : null;
    var normalisedHost = host.normalise();
    @SuppressWarnings("OptionalAssignedToNull")
    var optionalNormalisedPort = normalisedPort != null ? Optional.of(normalisedPort) : null;
    if (Objects.equals(normalisedUserInfo, userInfo)
        && normalisedHost.equals(host)
        && Objects.equals(optionalNormalisedPort, maybePort)) {
      return this;
    } else if (normalisedUserInfo == null) {
      return HostAndPort.of(normalisedHost, normalisedPort);
    } else {
      return new AuthorityValue(normalisedUserInfo, normalisedHost, optionalNormalisedPort);
    }
  }

  @Override
  public @Nullable UserInfo getUserInfo() {
    return userInfo;
  }

  @Override
  public Host getHost() {
    return host;
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
