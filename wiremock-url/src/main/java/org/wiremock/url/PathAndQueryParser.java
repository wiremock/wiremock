/*
 * Copyright (C) 2025 Thomas Akehurst
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

final class PathAndQueryParser implements StringParser<PathAndQuery> {

  static final PathAndQueryParser INSTANCE = new PathAndQueryParser();

  @Override
  public PathAndQuery parse(String stringForm) {
    try {
      var urlReference = UriReferenceParser.INSTANCE.parse(stringForm);
      if (urlReference instanceof PathAndQuery) {
        return (PathAndQuery) urlReference;
      } else {
        throw new IllegalPathAndQuery(stringForm);
      }
    } catch (IllegalUriPart illegalUriPart) {
      throw new IllegalPathAndQuery(stringForm, illegalUriPart);
    }
  }
}
