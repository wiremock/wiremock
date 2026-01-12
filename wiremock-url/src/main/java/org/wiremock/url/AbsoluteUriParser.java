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

final class AbsoluteUriParser implements StringParser<AbsoluteUri> {

  static final AbsoluteUriParser INSTANCE = new AbsoluteUriParser();

  @Override
  public AbsoluteUri parse(String uriString) throws IllegalAbsoluteUri {
    try {
      var uri = UriParser.INSTANCE.parse(uriString);
      if (uri instanceof AbsoluteUri absoluteUri) {
        return absoluteUri;
      } else {
        throw new IllegalAbsoluteUri(uriString);
      }
    } catch (IllegalUriPart illegalUriPart) {
      throw new IllegalAbsoluteUri(uriString, illegalUriPart);
    }
  }
}
