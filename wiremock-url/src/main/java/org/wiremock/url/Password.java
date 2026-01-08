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

/**
 * Represents the password portion of user information in a URI authority.
 *
 * <p>The password is the part after the colon in user information ({@code username:password}).
 * Passwords may contain percent-encoded characters.
 *
 * <p><strong>Security Warning:</strong> Including passwords in URIs is strongly discouraged as they
 * may be logged, cached, or exposed in various ways. This interface is provided for compatibility
 * with legacy systems only.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see UserInfo
 */
public interface Password extends PercentEncoded {

  /**
   * Parses a string into a password.
   *
   * @param password the string to parse
   * @return the parsed password
   * @throws IllegalPassword if the string is not a valid password
   */
  static Password parse(String password) throws IllegalPassword {
    return PasswordParser.INSTANCE.parse(password);
  }

  /**
   * Encodes a string into a valid password with proper percent-encoding.
   *
   * @param unencoded the unencoded string
   * @return the encoded password
   */
  static Password encode(String unencoded) {
    return PasswordParser.INSTANCE.encode(unencoded);
  }
}
