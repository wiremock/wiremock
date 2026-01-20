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

import static org.wiremock.url.Constants.*;

import java.util.regex.Pattern;

public final class QueryParser implements PercentEncodedStringParser<Query> {

  public static final QueryParser INSTANCE = new QueryParser();

  static final String queryRegex = "[^#" + alwaysIllegal + "]*";
  private final Pattern queryPattern = Pattern.compile("^" + queryRegex + "$");

  @Override
  public Class<Query> getType() {
    return Query.class;
  }

  @Override
  public Query parse(String stringForm) throws IllegalQuery {
    if (stringForm.isEmpty()) {
      return Query.EMPTY;
    } else if (queryPattern.matcher(stringForm).matches()) {
      return new QueryValue(stringForm);
    } else {
      throw new IllegalQuery(stringForm);
    }
  }

  static final boolean[] queryCharSet = combine(pcharCharSet, include('/', '?'));

  @Override
  public Query encode(String unencoded) {
    var result = Constants.encode(unencoded, queryCharSet);
    return new QueryValue(result, true);
  }
}
