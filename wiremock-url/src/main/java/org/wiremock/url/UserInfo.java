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

import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;

import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public interface UserInfo {

  static UserInfo parse(String userInfoString) {
    return UserInfoParser.INSTANCE.parse(userInfoString);
  }

  @Nullable UserInfo normalise();

  String username();

  @Nullable String password();
}

class UserInfoParser implements CharSequenceParser<UserInfo> {

  static final UserInfoParser INSTANCE = new UserInfoParser();

  final String userInfoRegex = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + "|:)*";

  private final Pattern userInfoPattern = Pattern.compile("^" + userInfoRegex + "$");

  @Override
  public UserInfo parse(CharSequence stringForm) {
    String userInfoStr = stringForm.toString();
    if (userInfoPattern.matcher(stringForm).matches()) {
      var components = userInfoStr.split(":", 2);
      var username = components[0];
      final String password;
      if (components.length == 2) {
        password = components[1];
      } else {
        password = null;
      }
      return new UserInfo(userInfoStr, username, password);
    } else {
      throw new IllegalUserInfo(userInfoStr);
    }
  }

  record UserInfo(String userInfo, @Override String username, @Override @Nullable String password)
      implements org.wiremock.url.UserInfo {

    @Override
    public String toString() {
      return userInfo;
    }

    @Override
    public org.wiremock.url.@Nullable UserInfo normalise() {
      if (!username.isEmpty() && password != null && password.isEmpty()) {
        return new UserInfo(username, username, null);
      } else if (username.isEmpty() && (password == null || password.isEmpty())) {
        return null;
      } else {
        return this;
      }
    }
  }
}
