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
 * Represents a path and query component combination, which is a type of relative reference.
 *
 * <p>A PathAndQuery consists of a path and an optional query, with no scheme, authority, or
 * fragment. It is typically used to represent the part of a URL after the authority.
 *
 * <p>Implementations must be immutable and thread-safe.
 */
public non-sealed interface SchemeRelativeUrl extends RelativeUrl, UrlWithAuthority {

  /**
   * Returns the authority component of this scheme relative url, which should always be present.
   *
   * @return the authority component
   */
  @Override
  Authority getAuthority();

  /**
   * Returns a normalised form of this scheme relative url.
   *
   * @return a normalised scheme relative url
   */
  @Override
  SchemeRelativeUrl normalise();

  @Override
  default SchemeRelativeUrl getSchemeRelativeUrl() {
    return this;
  }

  /**
   * Parses a string into a scheme relative url
   *
   * @param pathAndQuery the string to parse
   * @return the parsed path and query
   * @throws IllegalPathAndQuery if the string is not a valid path and query
   */
  static SchemeRelativeUrl parse(String pathAndQuery) throws IllegalPathAndQuery {
    return SchemeRelativeUrlParser.INSTANCE.parse(pathAndQuery);
  }
}
