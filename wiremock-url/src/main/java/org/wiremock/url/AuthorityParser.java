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

import static org.wiremock.url.HostParser.hostRegex;
import static org.wiremock.url.UserInfoParser.userInfoRegex;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class AuthorityParser implements StringParser<Authority> {

  public static final AuthorityParser INSTANCE = new AuthorityParser();

  static final String authorityRegex =
      "(?:(?<userInfo>"
          + userInfoRegex
          + ")@)?(?<host>"
          + hostRegex
          + ")(?<colonAndPort>:(?<port>[0-9]+)?)?";

  private final Pattern authorityPattern = Pattern.compile("^" + authorityRegex + "$");

  @Override
  public Authority parse(String stringForm) throws IllegalAuthority {
    var matcher = authorityPattern.matcher(stringForm);
    if (matcher.matches()) {
      return parse(matcher, stringForm);
    } else {
      throw new IllegalAuthority(stringForm);
    }
  }

  Authority parse(Matcher matcher, String rawAuthority) throws IllegalAuthority {
    try {
      String userInfoString = matcher.group("userInfo");
      var userInfo = userInfoString == null ? null : UserInfoParser.INSTANCE.parse(userInfoString);
      var hostString = matcher.group("host");
      var host = HostParser.INSTANCE.parse(hostString);
      Optional<Port> maybePort = extractPort(matcher);
      //noinspection OptionalAssignedToNull
      if (userInfo == null && (maybePort == null || maybePort.isPresent())) {
        //noinspection OptionalAssignedToNull
        return new HostAndPortValue(host, maybePort != null ? maybePort.orElse(null) : null);
      } else {
        return new AuthorityValue(userInfo, host, maybePort);
      }
    } catch (IllegalUriPart cause) {
      throw new IllegalAuthority(rawAuthority, cause);
    }
  }

  Authority of(@Nullable UserInfo userInfo, Host host, @Nullable Port port) {
    if (userInfo == null) {
      return new HostAndPortValue(host, port);
    } else {
      var portOptional = port != null ? Optional.of(port) : Optional.<Port>empty();
      return new AuthorityValue(userInfo, host, portOptional);
    }
  }

  private static @Nullable Optional<Port> extractPort(Matcher matcher) {
    String colonAndPort = matcher.group("colonAndPort");
    if (colonAndPort == null) {
      //noinspection OptionalAssignedToNull
      return null;
    } else {
      String portString = matcher.group("port");
      return portString != null ? Optional.of(Port.parse(portString)) : Optional.empty();
    }
  }
}
