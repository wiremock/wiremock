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

import static org.wiremock.url.Constants.*;

import java.util.regex.Pattern;

final class UsernameParser implements PercentEncodedStringParser<Username> {

  static final UsernameParser INSTANCE = new UsernameParser();

  final String usernameRegex = "(?:[" + unreserved + subDelims + "]|" + pctEncoded + ")*";

  private final Pattern usernamePattern = Pattern.compile("^" + usernameRegex + "$");

  @Override
  public Username parse(String stringForm) throws IllegalUsername {
    if (usernamePattern.matcher(stringForm).matches()) {
      return new UsernameValue(stringForm);
    } else {
      throw new IllegalUsername(stringForm);
    }
  }

  private static final boolean[] usernameCharSet = combine(unreservedCharSet, subDelimCharSet);

  @Override
  public Username encode(String unencoded) {
    String encoded = Constants.encode(unencoded, usernameCharSet);
    return new UsernameValue(encoded);
  }
}
