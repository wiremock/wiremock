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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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

    List<String> pairs = Arrays.stream(query.split("&")).collect(Collectors.toList());
    Builder<String, String> builder = ImmutableListMultimap.builder();
    for (String queryElement : pairs) {
      int firstEqualsIndex = queryElement.indexOf('=');
      if (firstEqualsIndex == -1) {
        builder.putAll(decode(queryElement), "");
      } else {
        String key = decode(queryElement.substring(0, firstEqualsIndex));
        String value = decode(queryElement.substring(firstEqualsIndex + 1));
        builder.putAll(key, value);
      }
    }

    return Maps.transformEntries(
        builder.build().asMap(), (key, values) -> new QueryParameter(key, new ArrayList<>(values)));
  }

  public static String getPath(String url) {
    return url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
  }

  public static String getPathAndQuery(String url) {
    return isAbsolute(url) ? url.substring(StringUtils.ordinalIndexOf(url, "/", 3)) : url;
  }

  private static boolean isAbsolute(String url) {
    return url.matches("^https?:\\/\\/.*");
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
