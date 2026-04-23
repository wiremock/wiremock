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

public final class SchemeRelativeUrlParser implements StringParser<SchemeRelativeUrl> {

  public static final SchemeRelativeUrlParser INSTANCE =
      new SchemeRelativeUrlParser(UriParser.INSTANCE);

  private final UriParser uriParser;

  public SchemeRelativeUrlParser(UriParser uriParser) {
    this.uriParser = uriParser;
  }

  @Override
  public Class<SchemeRelativeUrl> getType() {
    return SchemeRelativeUrl.class;
  }

  @Override
  public SchemeRelativeUrl parse(String stringForm) throws IllegalSchemeRelativeUrl {
    var uri = uriParser.parse(stringForm);
    if (uri instanceof SchemeRelativeUrl schemeRelativeUrl) {
      return schemeRelativeUrl;
    } else {
      throw new IllegalSchemeRelativeUrl(stringForm);
    }
  }
}
