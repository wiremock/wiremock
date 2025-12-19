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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;

import java.util.regex.Pattern;

public interface Password extends PercentEncoded {

  static Password parse(CharSequence password) throws IllegalPassword {
    return PasswordParser.INSTANCE.parse(password);
  }

  static Password encode(String unencoded) {
    return PasswordParser.INSTANCE.encode(unencoded);
  }
}

class PasswordParser implements PercentEncodedCharSequenceParser<Password> {

  static final PasswordParser INSTANCE = new PasswordParser();

  final String passwordRegex = "(?:[" + unreserved + subDelims + ":]|" + pctEncoded + ")*";

  private final Pattern passwordPattern = Pattern.compile("^" + passwordRegex + "$");

  @Override
  public Password parse(CharSequence stringForm) throws IllegalPassword {
    String passwordStr = stringForm.toString();
    if (passwordPattern.matcher(passwordStr).matches()) {
      return new Password(passwordStr);
    } else {
      throw new IllegalPassword(passwordStr);
    }
  }

  @Override
  public Password encode(String unencoded) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < unencoded.length(); i++) {
      char c = unencoded.charAt(i);
      if (isUnreserved(c) || isSubDelim(c) || c == ':') {
        result.append(c);
      } else {
        byte[] bytes = String.valueOf(c).getBytes(UTF_8);
        for (byte b : bytes) {
          result.append('%');
          result.append(String.format("%02X", b & 0xFF));
        }
      }
    }
    return new Password(result.toString());
  }

  private boolean isUnreserved(char c) {
    return (c >= 'A' && c <= 'Z')
        || (c >= 'a' && c <= 'z')
        || (c >= '0' && c <= '9')
        || c == '-'
        || c == '.'
        || c == '_'
        || c == '~';
  }

  private boolean isSubDelim(char c) {
    return c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')' || c == '*'
        || c == '+' || c == ',' || c == ';' || c == '=';
  }

  record Password(String password) implements org.wiremock.url.Password {

    @Override
    public String toString() {
      return password;
    }
  }
}
