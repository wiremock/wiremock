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
      "(?:(?<userInfo>"
          + UserInfoParser.INSTANCE.userInfoRegex
          + ")@)?(?<host>"
          + HostParser.INSTANCE.hostRegex
          + ")(?<colonAndPort>:(?<port>"
          + portRegex
          + ")?)?";

  private final Pattern authorityPattern = Pattern.compile("^" + authorityRegex + "$");

  @Override
  public Authority parse(CharSequence stringForm) throws IllegalAuthority {
    var matcher = authorityPattern.matcher(stringForm);
    if (matcher.matches()) {
      return parse(matcher, stringForm.toString());
    } else {
      throw new IllegalAuthority(stringForm.toString());
    }
  }

  Authority parse(Matcher matcher, String rawAuthority) throws IllegalAuthority {
    try {
      String userInfoString = matcher.group("userInfo");
      var userInfo = userInfoString == null ? null : UserInfoParser.INSTANCE.parse(userInfoString);
      var hostString = matcher.group("host");
      var host = HostParser.INSTANCE.parse(hostString);
      Optional<Optional<Port>> maybePort = extractPort(matcher);
      if (userInfo == null && !(maybePort.isPresent() && maybePort.get().isEmpty())) {
        return new HostAndPort(host, maybePort.flatMap(identity()).orElse(null));
      } else {
        return new AuthorityValue(userInfo, host, maybePort);
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
    public Optional<Optional<Port>> maybePort() {
      return port != null ? Optional.of(Optional.of(port)) : Optional.empty();
    }

    @Override
    public org.wiremock.url.HostAndPort withPort(@Nullable Port port) {
      if (Objects.equals(port, this.port)) {
        return this;
      } else {
        return new HostAndPort(host, port);
      }
    }

    @Override
    public org.wiremock.url.HostAndPort normalise() {
      var normalisedHost = host.normalise();
      var normalisedPort = port == null ? null : port.normalise();
      return normalised(normalisedHost, normalisedPort);
    }

    @Override
    public org.wiremock.url.HostAndPort normalise(Scheme canonicalScheme) {
      var normalisedHost = host.normalise();
      var normalisedPort = port == null ? null : port.normalise();
      if (Objects.equals(canonicalScheme.defaultPort(), normalisedPort)) {
        normalisedPort = null;
      }
      return normalised(normalisedHost, normalisedPort);
    }

    private HostAndPort normalised(Host normalisedHost, @Nullable Port normalisedPort) {
      return normalisedHost.equals(host) && Objects.equals(normalisedPort, port)
          ? this
          : new HostAndPort(normalisedHost, normalisedPort);
    }
  }
}
