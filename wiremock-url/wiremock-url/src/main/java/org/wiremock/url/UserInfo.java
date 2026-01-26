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

import org.jspecify.annotations.Nullable;
import org.wiremock.stringparser.ParsedString;

/**
 * Represents the user information component of a URI authority as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">RFC 3986 Section 3.2.1</a>.
 *
 * <p>User information typically consists of a username and optional password in the form {@code
 * username[:password]}. It appears before the host in a URI, separated by an {@code @} symbol.
 *
 * <p><strong>Security Note:</strong> Including passwords in URIs is deprecated due to security
 * concerns. Most modern protocols discourage this practice.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">RFC 3986 Section
 *     3.2.1</a>
 */
public interface UserInfo extends PercentEncoded<UserInfo>, ParsedString {

  /**
   * Parses a string into user info.
   *
   * @param userInfoString the string to parse
   * @return the parsed user info
   * @throws IllegalUserInfo if the string is not valid user info
   */
  static UserInfo parse(String userInfoString) throws IllegalUserInfo {
    return UserInfoParser.INSTANCE.parse(userInfoString);
  }

  /**
   * Encodes a string into valid user info with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded user info
   */
  static UserInfo encode(String unencoded) {
    return UserInfoParser.INSTANCE.encode(unencoded);
  }

  /**
   * Returns the username component.
   *
   * @return the username, never {@code null}
   */
  Username getUsername();

  /**
   * Returns the password component, or {@code null} if there is no password.
   *
   * @return the password, or {@code null} if absent
   */
  @Nullable Password getPassword();
}
