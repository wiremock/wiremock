/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

public class Urls {

  private Urls() {}

  public static Map<String, QueryParameter> splitQueryFromUrl(String url) {
    String queryPart =
        url.contains("?") && !url.endsWith("?") ? url.substring(url.indexOf('?') + 1) : null;

    return splitQuery(queryPart);
  }

  public static Map<String, QueryParameter> splitQuery(URI uri) {
    if (uri == null) {
      return Collections.emptyMap();
    }

    return splitQuery(uri.getRawQuery());
  }

  public static Map<String, QueryParameter> splitQuery(String query) {

    if (query == null) {
      return Collections.emptyMap();
    }

    Map<String, List<String>> queryParams = new HashMap<>();
    List<String> pairs = Arrays.stream(query.split("&")).collect(Collectors.toList());
    for (String queryElement : pairs) {
      int firstEqualsIndex = queryElement.indexOf('=');
      if (firstEqualsIndex == -1) {
        queryParams.put(decode(queryElement), Collections.singletonList(""));
      } else {
        String key = decode(queryElement.substring(0, firstEqualsIndex));
        String value = decode(queryElement.substring(firstEqualsIndex + 1));
        if (key.matches(".*\\[\\d+\\]")) {
          // Handle array format like ?id[0]=1&id[1]=2&id[2]=3
          int startIndex = key.indexOf('[');
          int endIndex = key.indexOf(']');
          int index = Integer.parseInt(key.substring(startIndex + 1, endIndex));
          key = key.substring(0, startIndex);
          List<String> values = queryParams.computeIfAbsent(key, paramName -> new ArrayList<>());
          while (values.size() <= index) {
            values.add("");
          }
          queryParams.put(key, values);
          values.set(index, value);
        } else {
          List<String> values = queryParams.computeIfAbsent(key, paramName -> new ArrayList<>());
          if (value.startsWith("[") && value.endsWith("]")) {
            // Handle format like key=[value1,value2,value3]
            String arrayValue =
                value.substring(1, value.length() - 1).replace("\"", "").replace("'", "");
            values.addAll(Arrays.asList(arrayValue.split(",")));
          } else if (value.contains(",")) {
            // Handle format like key=value1,value2,value3
            values.addAll(Arrays.asList(value.split(",")));
          } else if (value.contains("|")) {
            // Handle format like key=value1|value2|value3
            values.addAll(Arrays.asList(value.split("\\|")));
          } else {
            // Handle other formats
            values.add(value);
          }
          queryParams.put(key, values);
        }
      }
    }
    return queryParams.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> new QueryParameter(entry.getKey(), entry.getValue())));
  }

  public static String getPath(String url) {
    return url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
  }

  public static List<String> getPathSegments(String path) {
    return List.of(path.split("/"));
  }

  public static String urlToPathParts(URI uri) {
    List<String> uriPathNodes =
        Arrays.stream(uri.getPath().split("/"))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableList());
    int nodeCount = uriPathNodes.size();

    return nodeCount > 0 ? String.join("-", uriPathNodes) : "";
  }

  public static String decode(String encoded) {
    return URLDecoder.decode(encoded, UTF_8);
  }

  public static URL safelyCreateURL(String url) {
    try {
      return new URL(clean(url));
    } catch (MalformedURLException e) {
      return throwUnchecked(e, URL.class);
    }
  }

  // Workaround for a Jetty bug that appends "null" onto the end of the URL
  private static String clean(String url) {
    return url.matches(".*:[0-9]+null$") ? url.substring(0, url.length() - 4) : url;
  }

  public static int getPort(URL url) {
    if (url.getPort() == -1) {
      return url.getProtocol().equals("https") ? 443 : 80;
    }

    return url.getPort();
  }
}
