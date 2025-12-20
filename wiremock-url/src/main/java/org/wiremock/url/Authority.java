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

import java.util.Optional;
import org.jspecify.annotations.Nullable;

public interface Authority {

  @Nullable UserInfo userInfo();

  Host host();

  @Nullable Port port();

  /*
   * An Authority can legitimately be any of:
   * - `example.com` - host, no port
   * - `example.com:80` - host with port
   * - `example.com:` - host, empty port
   *
   * This method allows distinguishing between the first and third cases:
   * - `Optional.empty()` - no port
   * - `Optional.of(Optional.of(port))` - with port
   * - `Optional.of(Optional.empty())` - empty port
   */
  Optional<Optional<Port>> maybePort();

  HostAndPort hostAndPort();

  Authority withPort(@Nullable Port port);

  default Authority withoutPort() {
    return withPort(null);
  }

  Authority normalise();

  Authority normalise(Scheme canonicalScheme);

  static Authority parse(String authorityStr) throws IllegalAuthority {
    return AuthorityParser.INSTANCE.parse(authorityStr);
  }

  static Authority of(Host host) {
    return of(null, host, null);
  }

  static Authority of(Host host, @Nullable Port port) {
    return of(null, host, port);
  }

  static Authority of(@Nullable UserInfo userInfo, Host host) {
    return of(userInfo, host, null);
  }

  static Authority of(@Nullable UserInfo userInfo, Host host, @Nullable Port port) {
    if (userInfo == null) {
      return new AuthorityParser.HostAndPort(host, port);
    } else {
      var portOptional =
          port == null ? Optional.<Optional<Port>>empty() : Optional.of(Optional.of(port));
      return new AuthorityValue(userInfo, host, portOptional);
    }
  }
}
