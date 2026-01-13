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

final class AbsoluteUrlParser implements StringParser<AbsoluteUrl> {

  static final AbsoluteUrlParser INSTANCE = new AbsoluteUrlParser();

  @Override
  public AbsoluteUrl parse(String stringForm) throws IllegalAbsoluteUrl {
    var uri = UriParser.INSTANCE.parse(stringForm);
    if (uri instanceof AbsoluteUrl absoluteUrl) {
      return absoluteUrl;
    } else {
      throw new IllegalAbsoluteUrl(stringForm);
    }
  }
}
