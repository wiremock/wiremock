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

import static org.wiremock.url.Constants.alwaysIllegal;
import static org.wiremock.url.Constants.remove;
import static org.wiremock.url.QueryParser.queryCharSet;

import java.util.regex.Pattern;

public final class QueryParamKeyParser implements PercentEncodedStringParser<QueryParamKey> {

  public static final QueryParamKeyParser INSTANCE = new QueryParamKeyParser();

  private static final String queryParamKeyRegex = "[^#&=" + alwaysIllegal + "]*";
  private static final Pattern queryParamKeyPattern =
      Pattern.compile("^" + queryParamKeyRegex + "$");

  @Override
  public Class<QueryParamKey> getType() {
    return QueryParamKey.class;
  }

  @Override
  public QueryParamKey parse(String stringForm) {
    if (stringForm.isEmpty()) {
      return QueryParamKey.EMPTY;
    } else if (queryParamKeyPattern.matcher(stringForm).matches()) {
      return new QueryParamKeyValue(stringForm);
    } else {
      throw new IllegalSegment(stringForm);
    }
  }

  static final boolean[] queryParamKeyCharSet = remove(queryCharSet, '&', '=', '+');

  @Override
  public QueryParamKey encode(String unencoded) {
    return unencoded.isEmpty()
        ? QueryParamKey.EMPTY
        : new QueryParamKeyValue(Constants.encode(unencoded, queryParamKeyCharSet), true);
  }
}
