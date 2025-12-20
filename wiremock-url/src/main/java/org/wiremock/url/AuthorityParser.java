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
        return new HostAndPortValue(host, maybePort.flatMap(identity()).orElse(null));
      } else {
        return new AuthorityValue(userInfo, host, maybePort);
      }
    } catch (IllegalUrlPart cause) {
      throw new IllegalAuthority(rawAuthority, cause);
    }
  }

  Authority of(@Nullable UserInfo userInfo, Host host, @Nullable Port port) {
    if (userInfo == null) {
      return new HostAndPortValue(host, port);
    } else {
      var portOptional =
          port == null ? Optional.<Optional<Port>>empty() : Optional.of(Optional.of(port));
      return new AuthorityValue(userInfo, host, portOptional);
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
}
