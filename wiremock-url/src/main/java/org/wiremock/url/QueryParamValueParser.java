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

public final class QueryParamValueParser implements PercentEncodedStringParser<QueryParamValue> {

  public static final QueryParamValueParser INSTANCE = new QueryParamValueParser();

  private static final String queryParamValueRegex = "[^#&" + alwaysIllegal + "]*";
  private static final Pattern queryParamValuePattern =
      Pattern.compile("^" + queryParamValueRegex + "$");

  @Override
  public Class<QueryParamValue> getType() {
    return QueryParamValue.class;
  }

  @Override
  public QueryParamValue parse(String stringForm) {
    if (queryParamValuePattern.matcher(stringForm).matches()) {
      return new QueryParamValueValue(stringForm);
    } else {
      throw new IllegalSegment(stringForm);
    }
  }

  static final boolean[] queryParamValueCharSet = remove(queryCharSet, '&');

  @Override
  public QueryParamValue encode(String unencoded) {
    return new QueryParamValueValue(Constants.encode(unencoded, queryParamValueCharSet), true);
  }
}
