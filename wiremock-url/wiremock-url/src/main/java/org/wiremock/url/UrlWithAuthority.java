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

public sealed interface UrlWithAuthority extends Url permits SchemeRelativeUrl, AbsoluteUrl {

  /**
   * Returns the authority component of this URL.
   *
   * <p>URLs always have an authority component (unlike relative references and URNs).
   *
   * @return the authority component, never {@code null}
   */
  @Override
  Authority getAuthority();

  SchemeRelativeUrl getSchemeRelativeUrl();

  /**
   * Parses a string into a URL with an authority.
   *
   * @param url the string to parse
   * @return the parsed URI reference
   * @throws IllegalUrl if the string is not a valid URI reference
   */
  static UrlWithAuthority parse(String url) throws IllegalUrl {
    return UrlWithAuthorityParser.INSTANCE.parse(url);
  }
}
