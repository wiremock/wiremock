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

public final class PathParser implements PercentEncodedStringParser<Path> {

  public static final PathParser INSTANCE = new PathParser();

  static final String pathRegex = "[^#?" + alwaysIllegal + "]*";
  private final Pattern pathPattern = Pattern.compile("^" + pathRegex + "$");

  @Override
  public Class<Path> getType() {
    return Path.class;
  }

  @Override
  public Path parse(String stringForm) {
    if (stringForm.isEmpty()) {
      return Path.EMPTY;
    } else if (stringForm.equals("/")) {
      return Path.ROOT;
    } else if (pathPattern.matcher(stringForm).matches()) {
      return new PathValue(stringForm);
    } else {
      throw new IllegalPath(stringForm);
    }
  }

  Path construct(String stringForm) {
    if (stringForm.isEmpty()) {
      return Path.EMPTY;
    } else if (stringForm.equals("/")) {
      return Path.ROOT;
    } else {
      return new PathValue(stringForm);
    }
  }

  private static final boolean[] charactersToLeaveAsIs = include('/');

  static final boolean[] pathCharSet = combine(pcharCharSet, charactersToLeaveAsIs);

  @Override
  public Path encode(String unencoded) {
    if (unencoded.isEmpty()) {
      return Path.EMPTY;
    } else if (unencoded.equals("/")) {
      return Path.ROOT;
    } else {
      return new PathValue(PercentEncoding.encode(unencoded, pathCharSet), true);
    }
  }

  String normalisePercentEncoded(String unencoded) {
    String result = PercentEncoding.normalise(unencoded, pathCharSet, charactersToLeaveAsIs);
    return result != null ? result : unencoded;
  }
}
