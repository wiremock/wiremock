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
    this.host = Objects.requireNonNull(host);
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
    var normalisedHost = host.normalise();
    final Optional<Port> optionalNormalisedPort;
    if (maybePort != null && maybePort.isPresent()) {
      var normalisedPort = maybePort.get().normalise();
      optionalNormalisedPort = Optional.of(normalisedPort);
    } else {
      //noinspection OptionalAssignedToNull
      optionalNormalisedPort = null;
    }
    return buildNormalisedAuthority(userInfo, normalisedHost, optionalNormalisedPort);
  }

  @Override
  public Authority normalise(Scheme canonicalScheme) {
    var normalisedHost = host.normalise();
    Port port = getPort();
    var normalisedPort = port != null ? port.normalise() : null;
    final Optional<Port> optionalPort;
    if (normalisedPort == null
        || Objects.equals(normalisedPort, canonicalScheme.getDefaultPort())) {
      //noinspection OptionalAssignedToNull
      optionalPort = null;
    } else {
      optionalPort = Optional.of(normalisedPort);
    }

    return buildNormalisedAuthority(userInfo, normalisedHost, optionalPort);
  }

  private Authority buildNormalisedAuthority(
      @Nullable UserInfo normalisedUserInfo,
      Host normalisedHost,
      @Nullable Optional<Port> optionalNormalisedPort) {
    if (Objects.equals(normalisedUserInfo, userInfo)
        && normalisedHost.equals(host)
        && Objects.equals(optionalNormalisedPort, maybePort)) {
      return this;
    } else if (normalisedUserInfo == null) {
      @SuppressWarnings("OptionalAssignedToNull")
      var normalisedPort =
          optionalNormalisedPort != null ? optionalNormalisedPort.orElse(null) : null;
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
