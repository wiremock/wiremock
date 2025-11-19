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

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SystemKeyAuthoriser {

  private final Set<Pattern> regexes = new HashSet<>();

  public SystemKeyAuthoriser(Set<String> patterns) {
    if (patterns == null || patterns.isEmpty()) {
      patterns = Set.of("wiremock.*");
    }

    for (String pattern : patterns) {
      regexes.add(Pattern.compile(pattern, CASE_INSENSITIVE));
    }
  }

  public boolean isPermitted(String key) {
    for (Pattern regex : regexes) {
      if (regex.matcher(key).matches()) {
        return true;
      }
    }

    return false;
  }
}
