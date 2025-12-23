/*
 * Copyright (C) 2023-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.url.PathParams;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.wiremock.url.Path;
import org.wiremock.url.PathAndQuery;
import org.wiremock.url.Segment;

public class TemplatedUrlPath extends LinkedHashMap<String, String> implements Iterable<String> {

  private final Path originalPath;

  public TemplatedUrlPath(PathAndQuery url, PathParams pathParams) {
    this.originalPath = url.getPath();
    addAllPathSegments();
    putAll(pathParams);
  }

  private void addAllPathSegments() {
    final List<Segment> pathSegments = originalPath.getSegments();
    int i = 0;
    for (Segment pathNode : pathSegments) {
      if (!pathNode.isEmpty()) {
        String key = String.valueOf(i++);
        put(key, pathNode.toString());
      }
    }
  }

  @Override
  public String toString() {
    return originalPath.toString();
  }

  @Override
  public Iterator<String> iterator() {
    return originalPath.getSegments().stream().map(Object::toString).iterator();
  }
}
