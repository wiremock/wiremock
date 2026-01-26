/*
 * Copyright (C) 2026 Thomas Akehurst
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

/**
 * A base url is a url whose path is either empty or ends with a slash, and which has neither a
 * query nor a fragment.
 *
 * <p>You can always resolve a relative path (and query and fragment) onto it and it will
 * effectively be appended to the base url:
 *
 * <ul>
 *   <li>{@code https://example.com resolve some/path?q#f == https://example.com/some/path?q#f}
 *   <li>{@code https://example.com/base/path/ resolve some/path?q#f ==
 *       https://example.com/base/path/some/path?q#f}
 * </ul>
 */
public interface BaseUrl extends ServersideAbsoluteUrl {

  /**
   * Returns a normalised form of this base url
   *
   * @return a normalised base url
   */
  @Override
  BaseUrl normalise();

  /**
   * Returns this.
   *
   * @return this
   */
  @Override
  @Deprecated // no point ever calling on this subtype
  default BaseUrl getServersideAbsoluteUrl() {
    return this;
  }

  /**
   * Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Deprecated // no point ever calling on this subtype
  @Nullable
  default Query getQuery() {
    return null;
  }

  /**
   * Returns this.
   *
   * @return this
   */
  @Override
  @Deprecated // no point ever calling on this subtype
  default BaseUrl toBaseUrl() {
    return this;
  }

  /**
   * Parses a string into a base url.
   *
   * @param baseUrl the string to parse
   * @return the parsed base url
   * @throws IllegalBaseUrl if the string is not a valid base url
   */
  static BaseUrl parse(String baseUrl) throws IllegalBaseUrl {
    return BaseUrlParser.INSTANCE.parse(baseUrl);
  }

  static BaseUrl of(Scheme scheme, Authority authority) {
    return of(scheme, authority, Path.EMPTY);
  }

  static BaseUrl of(Scheme scheme, Authority authority, Path path) {
    return new BaseUrlValue(null, scheme, authority, path);
  }
}
