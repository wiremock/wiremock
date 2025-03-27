/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
import java.net.URI;
import java.util.ArrayList;

public class UrlPath extends ArrayList<String> {

  private final String originalPath;

  public UrlPath(String url) {
    originalPath = URI.create(url).getPath();
    Urls.getPathSegments(originalPath)
        .forEach(
            pathNode -> {
              if (isNotEmpty(pathNode)) {
                add(pathNode);
              }
            });
  }

  @Override
  public String toString() {
    return originalPath;
  }
}
