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

import static org.wiremock.url.Constants.*;

import java.util.regex.Pattern;

final class PasswordParser implements PercentEncodedStringParser<Password> {

  static final PasswordParser INSTANCE = new PasswordParser();

  final String passwordRegex = "(?:[" + unreserved + subDelims + ":]|" + pctEncoded + ")*";

  private final Pattern passwordPattern = Pattern.compile("^" + passwordRegex + "$");

  @Override
  public Password parse(String stringForm) throws IllegalPassword {
    if (passwordPattern.matcher(stringForm).matches()) {
      return new PasswordValue(stringForm);
    } else {
      throw new IllegalPassword(stringForm);
    }
  }

  private static final boolean[] passwordCharSet =
      combine(unreservedCharSet, subDelimCharSet, include(':'));

  @Override
  public Password encode(String unencoded) {
    var result = Constants.encode(unencoded, passwordCharSet);
    return new PasswordValue(result, true);
  }
}
