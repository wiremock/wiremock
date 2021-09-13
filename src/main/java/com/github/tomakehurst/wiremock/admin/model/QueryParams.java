/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Pair;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class QueryParams extends LinkedHashMap<String, List<String>> {

  public static final QueryParams EMPTY = new QueryParams();

  public static QueryParams single(String key, String... values) {
    return new QueryParams().add(key, values);
  }

  public QueryParams add(String key, String... values) {
    return add(key, Arrays.asList(values));
  }

  public QueryParams add(String key, List<String> values) {
    put(key, values);
    return this;
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "";
    }

    return "?"
        + entrySet().stream()
            .flatMap(
                entry -> entry.getValue().stream().map(value -> Pair.pair(entry.getKey(), value)))
            .map(pair -> pair.a + "=" + pair.b)
            .collect(Collectors.joining("&"));
  }
}
