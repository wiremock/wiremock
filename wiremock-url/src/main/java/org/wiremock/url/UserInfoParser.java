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

class UserInfoParser implements PercentEncodedStringParser<UserInfo> {

  static final UserInfoParser INSTANCE = new UserInfoParser();

  static final String userInfoRegex = "(?:[" + unreserved + subDelims + ":]|" + pctEncoded + ")*";

  private final Pattern userInfoPattern = Pattern.compile("^" + userInfoRegex + "$");

  @Override
  public UserInfo parse(String stringForm) {
    if (userInfoPattern.matcher(stringForm).matches()) {
      return new UserInfoValue(stringForm);
    } else {
      throw new IllegalUserInfo(stringForm);
    }
  }

  static final boolean[] usernameCharSet = combine(unreservedCharSet, subDelimCharSet);

  static final boolean[] userInfoCharSet = combine(usernameCharSet, include(':'));

  @Override
  public UserInfo encode(String unencoded) {
    String encoded = Constants.encode(unencoded, userInfoCharSet);
    return parse(encoded);
  }
}
