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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Strings {

  static String transform(
      String original, Pattern pattern, Function<String, String> transformMatched) {
    return transform(original, pattern, transformMatched, Function.identity());
  }

  static String transform(
      String original,
      Pattern pattern,
      Function<String, String> transformMatched,
      Function<String, String> transformUnmatched) {
    StringBuilder result = new StringBuilder();
    Matcher matcher = pattern.matcher(original);
    int lastEnd = 0;

    while (matcher.find()) {
      // Transform the part before the match
      result.append(transformUnmatched.apply(original.substring(lastEnd, matcher.start())));
      // Transform the match
      result.append(transformMatched.apply(matcher.group()));
      lastEnd = matcher.end();
    }

    // Transform the remaining part
    result.append(transformUnmatched.apply(original.substring(lastEnd)));

    return result.toString();
  }

  private Strings() {
    throw new UnsupportedOperationException("Not instantiable");
  }
}
