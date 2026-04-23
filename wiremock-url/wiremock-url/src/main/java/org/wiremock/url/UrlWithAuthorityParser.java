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

import org.wiremock.stringparser.StringParser;

public class UrlWithAuthorityParser implements StringParser<UrlWithAuthority> {

  public static final UrlWithAuthorityParser INSTANCE =
      new UrlWithAuthorityParser(UriParser.INSTANCE);

  private final UriParser uriParser;

  public UrlWithAuthorityParser(UriParser uriParser) {
    this.uriParser = uriParser;
  }

  @Override
  public Class<UrlWithAuthority> getType() {
    return UrlWithAuthority.class;
  }

  @Override
  public UrlWithAuthority parse(String url) {
    var uri = uriParser.parse(url);
    if (uri instanceof UrlWithAuthority absoluteUri) {
      return absoluteUri;
    } else {
      if (url.contains(":")) {
        throw new IllegalAbsoluteUrl(url);
      } else {
        throw new IllegalRelativeUrl(url);
      }
    }
  }
}
