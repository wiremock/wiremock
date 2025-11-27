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
          + ")(:(?<port>"
          + portRegex
          + "))?";

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
      var userInfo = userInfoString == null ? null : new UserInfoParser.UserInfo(userInfoString);
      var hostString = matcher.group("host");
      var host = new HostParser.Host(hostString);
      String portString = matcher.group("port");
      var port = portString == null ? null : Port.parse(portString);
      return new AuthorityParser.Authority(userInfo, host, port);
    } catch (IllegalUrlPart cause) {
      throw new IllegalAuthority(rawAuthority, cause);
    }
  }

  record Authority(@Nullable @Override UserInfo userInfo, Host host, @Nullable @Override Port port)
      implements org.wiremock.url.Authority {

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      if (userInfo != null) {
        result.append(userInfo).append('@');
      }
      result.append(host);
      if (port != null) {
        result.append(':').append(port);
      }
      return result.toString();
    }
  }
}
