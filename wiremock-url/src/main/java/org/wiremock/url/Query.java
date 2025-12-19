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

import static java.nio.charset.StandardCharsets.UTF_8;
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

  final String queryRegex = "[^#" + alwaysIllegal + "]*";
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

  @Override
  public Query encode(String unencoded) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < unencoded.length(); i++) {
      char c = unencoded.charAt(i);
      if (isUnreserved(c) || isSubDelim(c) || c == ':' || c == '@' || c == '/' || c == '?') {
        result.append(c);
      } else {
        byte[] bytes = String.valueOf(c).getBytes(UTF_8);
        for (byte b : bytes) {
          result.append('%');
          result.append(String.format("%02X", b & 0xFF));
        }
      }
    }
    return new Query(result.toString());
  }

  private boolean isUnreserved(char c) {
    return (c >= 'A' && c <= 'Z')
        || (c >= 'a' && c <= 'z')
        || (c >= '0' && c <= '9')
        || c == '-'
        || c == '.'
        || c == '_'
        || c == '~';
  }

  private boolean isSubDelim(char c) {
    return c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')' || c == '*'
        || c == '+' || c == ',' || c == ';' || c == '=';
  }

  record Query(String query) implements org.wiremock.url.Query {

    @Override
    public String toString() {
      return query;
    }

    private static final boolean[] queryCharSet = combine(pcharCharSet, include('/', '?'));

    private static final boolean[] specialSchemeQueryCharSet = remove(queryCharSet, '\'');

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
