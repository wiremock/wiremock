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

import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelimCharSet;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;
import static org.wiremock.url.Constants.unreservedCharSet;

import java.util.regex.Pattern;

public interface Username extends PercentEncoded {

  static Username parse(CharSequence username) throws IllegalUsername {
    return UsernameParser.INSTANCE.parse(username);
  }

  static Username encode(String unencoded) {
    return UsernameParser.INSTANCE.encode(unencoded);
  }
}

class UsernameParser implements PercentEncodedCharSequenceParser<Username> {

  static final UsernameParser INSTANCE = new UsernameParser();

  final String usernameRegex = "(?:[" + unreserved + subDelims + "]|" + pctEncoded + ")*";

  private final Pattern usernamePattern = Pattern.compile("^" + usernameRegex + "$");

  @Override
  public Username parse(CharSequence stringForm) throws IllegalUsername {
    String usernameStr = stringForm.toString();
    if (usernamePattern.matcher(usernameStr).matches()) {
      return new Username(usernameStr);
    } else {
      throw new IllegalUsername(usernameStr);
    }
  }

  private static final boolean[] usernameCharSet = combine(unreservedCharSet, subDelimCharSet);

  @Override
  public Username encode(String unencoded) {
    String encoded = Constants.encode(unencoded, usernameCharSet);
    return new Username(encoded);
  }

  record Username(String username) implements org.wiremock.url.Username {

    @Override
    public String toString() {
      return username;
    }
  }
}
