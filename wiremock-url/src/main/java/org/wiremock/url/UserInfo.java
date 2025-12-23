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

import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelimCharSet;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;
import static org.wiremock.url.Constants.unreservedCharSet;

import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public interface UserInfo extends PercentEncoded {

  static UserInfo parse(String userInfoString) {
    return UserInfoParser.INSTANCE.parse(userInfoString);
  }

  static UserInfo encode(String unencoded) {
    return UserInfoParser.INSTANCE.encode(unencoded);
  }

  @Nullable UserInfo normalise();

  Username username();

  @Nullable Password password();
}

class UserInfoParser implements PercentEncodedCharSequenceParser<UserInfo> {

  static final UserInfoParser INSTANCE = new UserInfoParser();

  static final String userInfoRegex = "(?:[" + unreserved + subDelims + ":]|" + pctEncoded + ")*";

  private final Pattern userInfoPattern = Pattern.compile("^" + userInfoRegex + "$");

  @Override
  public UserInfo parse(CharSequence stringForm) {
    String userInfoStr = stringForm.toString();
    if (userInfoPattern.matcher(stringForm).matches()) {
      var components = userInfoStr.split(":", 2);
      var username = new UsernameValue(components[0]);
      final Password password;
      if (components.length == 2) {
        password = new PasswordValue(components[1]);
      } else {
        password = null;
      }
      return new UserInfoValue(userInfoStr, username, password);
    } else {
      throw new IllegalUserInfo(userInfoStr);
    }
  }

  private static final boolean[] userInfoCharSet =
      combine(unreservedCharSet, subDelimCharSet, include(':'));

  @Override
  public UserInfo encode(String unencoded) {
    String encoded = Constants.encode(unencoded, userInfoCharSet);
    return parse(encoded);
  }
}
