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

import static java.util.function.Function.identity;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
final class AuthorityValue implements Authority {

  private final @Nullable UserInfo userInfo;
  private final Host host;
  private final Optional<Optional<Port>> maybePort;

  AuthorityValue(@Nullable UserInfo userInfo, Host host, Optional<Optional<Port>> maybePort) {
    this.userInfo = userInfo;
    this.host = host;
    this.maybePort = maybePort;
  }

  @Override
  public @Nullable Port port() {
    return maybePort.flatMap(p -> p).orElse(null);
  }

  @Override
  public Optional<Optional<Port>> maybePort() {
    return maybePort;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (userInfo != null) {
      result.append(userInfo).append('@');
    }
    result.append(host);

    maybePort.ifPresent(
        port -> {
          result.append(':');
          port.ifPresent(result::append);
        });
    return result.toString();
  }

  @Override
  public HostAndPort hostAndPort() {
    return new AuthorityParser.HostAndPort(host, port());
  }

  @Override
  public Authority withPort(@Nullable Port port) {
    Optional<Optional<Port>> newPort =
        port != null ? Optional.of(Optional.of(port)) : Optional.empty();
    if (newPort.equals(maybePort)) {
      return this;
    } else {
      return new AuthorityValue(
          userInfo, host, newPort);
    }
  }

  @Override
  public Authority normalise() {
    var normalisedHost = host.normalise();
    final Optional<Optional<Port>> normalisedPort;
    if (maybePort.isEmpty() || maybePort.get().isPresent()) {
      normalisedPort = maybePort;
    } else {
      normalisedPort = Optional.empty();
    }
    var normalisedUserInfo = userInfo != null ? userInfo.normalise() : null;
    if (normalisedHost.equals(host)
        && normalisedPort.equals(maybePort)
        && Objects.equals(normalisedUserInfo, userInfo)) {
      return this;
    } else if (normalisedUserInfo == null) {
      return new AuthorityParser.HostAndPort(
          normalisedHost, normalisedPort.flatMap(identity()).orElse(null));
    } else {
      return new AuthorityValue(normalisedUserInfo, normalisedHost, normalisedPort);
    }
  }

  @Override
  public Authority normalise(Scheme canonicalScheme) {
    var normalisedHost = host.normalise();
    Port port = port();
    var normalisedPort = port == null ? null : port.normalise();
    final Optional<Optional<Port>> normalisedPort2;
    if (normalisedPort == null || Objects.equals(normalisedPort, canonicalScheme.defaultPort())) {
      normalisedPort = null;
      normalisedPort2 = Optional.empty();
    } else {
      normalisedPort2 = Optional.of(Optional.of(normalisedPort));
    }

    var normalisedUserInfo = Optional.ofNullable(userInfo).map(UserInfo::normalise).orElse(null);
    if (normalisedHost.equals(host)
        && normalisedPort2.equals(maybePort)
        && Objects.equals(normalisedUserInfo, userInfo)) {
      return this;
    } else if (normalisedUserInfo == null) {
      return new AuthorityParser.HostAndPort(normalisedHost, normalisedPort);
    } else {
      return new AuthorityValue(normalisedUserInfo, normalisedHost, normalisedPort2);
    }
  }

  @Override
  public @Nullable UserInfo userInfo() {
    return userInfo;
  }

  @Override
  public Host host() {
    return host;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Authority that)) {
      return false;
    }
    return Objects.equals(this.userInfo(), that.userInfo())
        && Objects.equals(this.host(), that.host())
        && Objects.equals(this.maybePort(), that.maybePort());
  }

  @Override
  public int hashCode() {
    return Objects.hash(userInfo, host, maybePort);
  }
}
