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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

class AuthorityParser implements CharSequenceParser<Authority> {

  public static final AuthorityParser INSTANCE = new AuthorityParser();

  final String portRegex = "[0-9]+";

  final String authorityRegex =
      "((?<userInfo>"
          + UserInfoParser.INSTANCE.userInfoRegex
          + ")@)?(?<host>"
          + HostParser.INSTANCE.hostRegex
          + ")(?<colonAndPort>:(?<port>"
          + portRegex
          + ")?)?";

  private final Pattern authorityPattern = Pattern.compile("^" + authorityRegex + "$");

  @Override
  public org.wiremock.url.Authority parse(CharSequence stringForm) throws IllegalAuthority {
    var matcher = authorityPattern.matcher(stringForm);
    if (matcher.matches()) {
      return parse(matcher, stringForm.toString());
    } else {
      throw new IllegalAuthority(stringForm.toString());
    }
  }

  org.wiremock.url.Authority parse(Matcher matcher, String rawAuthority) throws IllegalAuthority {
    try {
      String userInfoString = matcher.group("userInfo");
      var userInfo = userInfoString == null ? null : UserInfoParser.INSTANCE.parse(userInfoString);
      var hostString = matcher.group("host");
      var host = new HostParser.Host(hostString);
      Optional<Optional<Port>> maybePort = extractPort(matcher);
      if (userInfo == null && !(maybePort.isPresent() && maybePort.get().isEmpty())) {
        return new HostAndPort(host, maybePort.flatMap(identity()).orElse(null));
      } else {
        return new Authority(userInfo, host, maybePort);
      }
    } catch (IllegalUrlPart cause) {
      throw new IllegalAuthority(rawAuthority, cause);
    }
  }

  private static Optional<Optional<Port>> extractPort(Matcher matcher) {
    String colonAndPort = matcher.group("colonAndPort");
    if (colonAndPort == null) {
      return Optional.empty();
    } else {
      String portString = matcher.group("port");
      Optional<Port> port =
          portString == null ? Optional.empty() : Optional.of(Port.parse(portString));
      return Optional.of(port);
    }
  }

  record Authority(
      @Nullable @Override UserInfo userInfo,
      @Override Host host,
      Optional<Optional<Port>> maybePort)
      implements org.wiremock.url.Authority {

    @Override
    public @Nullable Port port() {
      return maybePort.flatMap(p -> p).orElse(null);
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
      return new HostAndPort(host, port());
    }

    @Override
    public org.wiremock.url.Authority withPort(@Nullable Port port) {
      return new Authority(userInfo, host, Optional.of(Optional.ofNullable(port)));
    }

    @Override
    public org.wiremock.url.Authority normalise() {
      final Optional<Optional<Port>> normalisedPort;
      if (maybePort.isEmpty() || maybePort.get().isPresent()) {
        normalisedPort = maybePort;
      } else {
        normalisedPort = Optional.empty();
      }
      var normalisedUserInfo = userInfo != null ? userInfo.normalise() : null;
      if (normalisedPort.equals(maybePort) && Objects.equals(normalisedUserInfo, userInfo)) {
        return this;
      } else {
        return new Authority(normalisedUserInfo, host, normalisedPort);
      }
    }

    @Override
    public org.wiremock.url.Authority normalise(Scheme canonicalScheme) {
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
      if (normalisedPort2.equals(maybePort) && Objects.equals(normalisedUserInfo, userInfo)) {
        return this;
      } else if (normalisedUserInfo == null) {
        return new HostAndPort(host, normalisedPort);
      } else {
        return new Authority(normalisedUserInfo, host, normalisedPort2);
      }
    }
  }

  record HostAndPort(@Override Host host, @Nullable @Override Port port)
      implements org.wiremock.url.HostAndPort {
    @Override
    public String toString() {
      if (port != null) {
        return host + ":" + port;
      } else {
        return host.toString();
      }
    }

    @Override
    public org.wiremock.url.HostAndPort withPort(@Nullable Port port) {
      return new HostAndPort(host, port);
    }

    @Override
    public org.wiremock.url.HostAndPort normalise() {
      var normalisedPort = port == null ? null : port.normalise();
      return normalisedPort == port ? this : new HostAndPort(host, normalisedPort);
    }

    @Override
    public org.wiremock.url.HostAndPort normalise(Scheme canonicalScheme) {
      if (port != null && canonicalScheme.defaultPort() == port) {
        return new HostAndPort(host, null);
      } else {
        return this;
      }
    }
  }
}
