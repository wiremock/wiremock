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

public final class OpaqueUriParser implements StringParser<OpaqueUri> {

  public static final OpaqueUriParser INSTANCE = new OpaqueUriParser(UriParser.INSTANCE);

  private final UriParser uriParser;

  public OpaqueUriParser(UriParser uriParser) {
    this.uriParser = uriParser;
  }

  @Override
  public Class<OpaqueUri> getType() {
    return OpaqueUri.class;
  }

  @Override
  public OpaqueUri parse(String stringForm) throws IllegalOpaqueUri {
    var uri = uriParser.parse(stringForm);
    if (uri instanceof OpaqueUri opaqueUri) {
      return opaqueUri;
    } else {
      throw new IllegalOpaqueUri(stringForm);
    }
  }
}
