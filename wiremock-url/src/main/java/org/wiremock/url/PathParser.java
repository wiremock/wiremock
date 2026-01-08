/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static org.wiremock.url.Constants.alwaysIllegal;
import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.pcharCharSet;

import java.util.regex.Pattern;

final class PathParser implements PercentEncodedStringParser<Path> {

  static final PathParser INSTANCE = new PathParser();

  static final String pathRegex = "[^#?" + alwaysIllegal + "]*";
  private final Pattern pathPattern = Pattern.compile("^" + pathRegex + "$");

  @Override
  public PathValue parse(String stringForm) {
    if (pathPattern.matcher(stringForm).matches()) {
      return new PathValue(stringForm);
    } else {
      throw new IllegalPath(stringForm);
    }
  }

  private static final boolean[] charactersToLeaveAsIs = include('/');

  private static final boolean[] pathCharSet = combine(pcharCharSet, charactersToLeaveAsIs);

  @Override
  public Path encode(String unencoded) {
    return parse(Constants.encode(unencoded, pathCharSet));
  }

  String encode2(String unencoded) {
    String result = Constants.normalise(unencoded, pathCharSet, charactersToLeaveAsIs);
    return result != null ? result : unencoded;
  }
}
