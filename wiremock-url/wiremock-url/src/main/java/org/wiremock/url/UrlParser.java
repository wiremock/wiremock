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

import org.wiremock.stringparser.StringParser;

public final class UrlParser implements StringParser<Url> {

  public static final UrlParser INSTANCE = new UrlParser(UriParser.INSTANCE);

  private final UriParser uriParser;

  public UrlParser(UriParser uriParser) {
    this.uriParser = uriParser;
  }

  @Override
  public Class<Url> getType() {
    return Url.class;
  }

  @Override
  public Url parse(String stringForm) {
    var uri = uriParser.parse(stringForm);
    if (uri instanceof Url url) {
      return url;
    } else {
      throw new IllegalUrl(stringForm, "Illegal url: `" + uri + "`; a url has an authority");
    }
  }
}
