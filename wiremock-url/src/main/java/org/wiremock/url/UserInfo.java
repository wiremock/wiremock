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

import java.util.regex.Pattern;

import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;

public interface UserInfo {

  static UserInfo parse(String userInfoString) {
    return UserInfoParser.INSTANCE.parse(userInfoString);
  }
}

class UserInfoParser implements CharSequenceParser<UserInfo> {

  static final UserInfoParser INSTANCE = new UserInfoParser();

  final String userInfoRegex = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + "|:)*";

  private final Pattern userInfoPattern = Pattern.compile("^" + userInfoRegex + "$");

  @Override
  public UserInfo parse(CharSequence stringForm) {
    if (userInfoPattern.matcher(stringForm).matches()) {
      return new UserInfo(stringForm.toString());
    } else {
      throw new IllegalUserInfo(stringForm.toString());
    }
  }

  record UserInfo(String userInfo) implements org.wiremock.url.UserInfo {
    @Override
    public String toString() {
      return userInfo;
    }
  }
}
