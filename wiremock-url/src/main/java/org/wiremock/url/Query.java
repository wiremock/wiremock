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

import static org.wiremock.url.Constants.alwaysIllegal;
import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.pcharCharSet;
import static org.wiremock.url.Constants.remove;
import static org.wiremock.url.Scheme.specialSchemes;

import java.util.regex.Pattern;

public interface Query extends PercentEncoded {
  static Query parse(CharSequence query) throws IllegalQuery {
    return QueryParser.INSTANCE.parse(query);
  }

  static Query encode(String unencoded) {
    return QueryParser.INSTANCE.encode(unencoded);
  }

  default Query normalise() {
    return normalise(Scheme.http);
  }

  Query normalise(Scheme scheme);
}

class QueryParser implements PercentEncodedCharSequenceParser<Query> {

  static final QueryParser INSTANCE = new QueryParser();

  static final String queryRegex = "[^#" + alwaysIllegal + "]*";
  private final Pattern queryPattern = Pattern.compile("^" + queryRegex + "$");

  @Override
  public Query parse(CharSequence stringForm) throws IllegalQuery {
    String queryStr = stringForm.toString();
    if (queryPattern.matcher(queryStr).matches()) {
      return new QueryParser.Query(queryStr);
    } else {
      throw new IllegalQuery(queryStr);
    }
  }

  private static final boolean[] queryCharSet = combine(pcharCharSet, include('/', '?'));

  private static final boolean[] specialSchemeQueryCharSet = remove(queryCharSet, '\'');

  @Override
  public Query encode(String unencoded) {
    var result = Constants.encode(unencoded, queryCharSet);
    return new Query(result);
  }

  record Query(String query) implements org.wiremock.url.Query {

    @Override
    public String toString() {
      return query;
    }

    @Override
    public org.wiremock.url.Query normalise(Scheme scheme) {
      boolean[] charactersThatDoNotNeedEncoding =
          specialSchemes.contains(scheme) ? specialSchemeQueryCharSet : queryCharSet;
      String result = Constants.normalise(query, charactersThatDoNotNeedEncoding);

      if (result == null) {
        return this;
      } else {
        return new Query(result);
      }
    }
  }
}
