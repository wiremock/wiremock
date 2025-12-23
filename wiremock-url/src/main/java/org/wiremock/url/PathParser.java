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

import static org.wiremock.url.Constants.alwaysIllegal;
import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.subDelimCharSet;
import static org.wiremock.url.Constants.unreservedCharSet;

import java.util.regex.Pattern;

final class PathParser implements PercentEncodedCharSequenceParser<Path> {

  static final PathParser INSTANCE = new PathParser();

  static final String pathRegex = "[^#?" + alwaysIllegal + "]*";
  private final Pattern pathPattern = Pattern.compile("^" + pathRegex + "$");

  @Override
  public Path parse(CharSequence stringForm) {
    String pathStr = stringForm.toString();
    if (pathPattern.matcher(pathStr).matches()) {
      return new PathValue(pathStr);
    } else {
      throw new IllegalPath(pathStr);
    }
  }

  private static final boolean[] pathCharSet =
      combine(unreservedCharSet, subDelimCharSet, include(':', '@', '/'));

  @Override
  public Path encode(String unencoded) {
    return parse(Constants.encode(unencoded, pathCharSet));
  }
}
