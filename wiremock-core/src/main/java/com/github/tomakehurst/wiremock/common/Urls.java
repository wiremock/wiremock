/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.wiremock.url.PathAndQuery;
import org.wiremock.url.Query;

public class Urls {

  private Urls() {}

  public static Map<String, QueryParameter> toQueryParameterMap(Query query) {
    return query.asDecodedMap().entrySet().stream()
        .collect(
            Collectors.toMap(Entry::getKey, e -> new QueryParameter(e.getKey(), e.getValue())));
  }

  public static String urlToPathParts(PathAndQuery uri) {
    List<String> uriPathNodes =
        uri.getPath().getSegments().stream()
            .filter(s -> !s.isEmpty())
            .map(Object::toString)
            .toList();
    int nodeCount = uriPathNodes.size();

    return nodeCount > 0 ? String.join("-", uriPathNodes) : "";
  }
}
