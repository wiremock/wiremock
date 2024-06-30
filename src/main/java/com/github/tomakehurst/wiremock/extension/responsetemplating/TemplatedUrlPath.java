/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.common.Strings.isNotEmpty;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class TemplatedUrlPath extends LinkedHashMap<String, String> implements Iterable<String> {

  private final String originalPath;

  public TemplatedUrlPath(String url, PathParams pathParams) {
    this.originalPath = Urls.getPath(url);
    addAllPathSegments();
    putAll(pathParams);
  }

  private void addAllPathSegments() {
    final List<String> pathSegments = Urls.getPathSegments(originalPath);
    int i = 0;
    for (String pathNode : pathSegments) {
      if (isNotEmpty(pathNode)) {
        String key = String.valueOf(i++);
        put(key, pathNode);
      }
    }
  }

  @Override
  public String toString() {
    return originalPath;
  }

  @Override
  public Iterator<String> iterator() {
    return Urls.getPathSegments(originalPath).iterator();
  }
}
