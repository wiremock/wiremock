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
import static org.wiremock.url.Scheme.specialSchemes;

import java.net.URLDecoder;
import java.util.regex.Pattern;

public interface Query extends PercentEncoded {
  static Query parse(CharSequence query) throws IllegalQuery {
    return QueryParser.INSTANCE.parse(query);
  }

  default Query normalise() {
    return normalise(Scheme.http);
  }

  Query normalise(Scheme scheme);
}

class QueryParser implements CharSequenceParser<Query> {

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
      try {
        return URLDecoder.decode(query, UTF_8);
      } catch (IllegalArgumentException e) {
        return query;
      }
    }

    @Override
    public org.wiremock.url.Query normalise(Scheme scheme) {
      StringBuilder result = new StringBuilder();
      boolean changed = false;

      boolean specialScheme = specialSchemes.contains(scheme);

      for (int i = 0; i < query.length(); i++) {
        char c = query.charAt(i);

        // Preserve already percent-encoded sequences
        if (c == '%'
            && i + 2 < query.length()
            && isHexDigit(query.charAt(i + 1))
            && isHexDigit(query.charAt(i + 2))) {
          result.append(c).append(query.charAt(i + 1)).append(query.charAt(i + 2));
          i += 2;
          continue;
        }

        // Check if character needs encoding per WhatWG query percent-encode set
        if (shouldPercentEncodeInQuery(c, specialScheme)) {
          // Encode as UTF-8 bytes
          byte[] bytes = String.valueOf(c).getBytes(UTF_8);
          for (byte b : bytes) {
            result.append('%');
            result.append(String.format("%02X", b & 0xFF));
          }
          changed = true;
        } else {
          result.append(c);
        }
      }

      if (!changed) {
        return this;
      } else {
        return new Query(result.toString());
      }
    }

    private boolean isHexDigit(char c) {
      return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private boolean shouldPercentEncodeInQuery(char c, boolean specialScheme) {
      if (c <= 0x1F) return true; // C0 controls
      if (c == 0x20) return true; // space
      if (c == '"') return true; // 0x22
      if (c == '#') return true; // 0x23
      // WhatWG URL makes `'` a special case for its magic schemes
      if (specialScheme && c == '\'') return true; // 0x27
      if (c == '<') return true; // 0x3C
      if (c == '>') return true; // 0x3E
      return c > 0x7E; // non-ASCII
    }
  }
}
