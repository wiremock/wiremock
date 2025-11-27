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

import java.util.regex.Pattern;

public interface Query extends PctEncoded {
  static Query parse(CharSequence query) throws IllegalQuery {
    return QueryParser.INSTANCE.parse(query);
  }
}

class QueryParser implements CharSequenceParser<Query> {

  static final QueryParser INSTANCE = new QueryParser();

  final String queryRegex = "[^#]*";
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

  record Query(String query) implements org.wiremock.url.Query {

    @Override
    public int length() {
      return query.length();
    }

    @Override
    public char charAt(int index) {
      return query.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return query.subSequence(start, end);
    }

    @Override
    public String toString() {
      return query;
    }

    @Override
    public String decode() {
      throw new UnsupportedOperationException();
    }
  }
}
