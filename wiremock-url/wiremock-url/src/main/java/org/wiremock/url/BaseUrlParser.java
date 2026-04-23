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

public class BaseUrlParser implements StringParser<BaseUrl> {

  public static final BaseUrlParser INSTANCE = new BaseUrlParser(UriParser.INSTANCE);

  private final UriParser uriParser;

  public BaseUrlParser(UriParser uriParser) {
    this.uriParser = uriParser;
  }

  @Override
  public Class<BaseUrl> getType() {
    return BaseUrl.class;
  }

  @Override
  public BaseUrl parse(String stringForm) throws IllegalBaseUrl {
    var uri = uriParser.parse(stringForm);
    if (uri instanceof BaseUrl baseUrl) {
      return baseUrl;
    } else {
      throw new IllegalBaseUrl(
          stringForm,
          "Illegal base url: `"
              + stringForm
              + "`; path must be a base path (empty or end in `/`), query must be null, fragment must be null");
    }
  }
}
