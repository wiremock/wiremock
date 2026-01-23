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

import java.util.regex.Pattern;
import org.wiremock.stringparser.StringParser;

public final class PathAndQueryParser implements StringParser<PathAndQuery> {

  public static final PathAndQueryParser INSTANCE = new PathAndQueryParser();

  @Override
  public Class<PathAndQuery> getType() {
    return PathAndQuery.class;
  }

  private static final Pattern regex =
      Pattern.compile("^(?<path>/[^?#]*|)(?:\\?(?<query>[^#]*))?$");

  @Override
  public PathAndQuery parse(String stringForm) {
    try {
      var result = regex.matcher(stringForm);
      if (!result.matches()) {
        throw new IllegalPathAndQuery(stringForm);
      }

      var path = PathParser.INSTANCE.parse(result.group("path"));

      var queryString = result.group("query");
      var query = queryString == null ? null : Query.parse(queryString);

      return PathAndQuery.of(path, query);
    } catch (IllegalUriPart illegalPart) {
      throw new IllegalUri(stringForm, illegalPart);
    }
  }
}
