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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.wiremock.url.Constants.multiplePctEncodedPattern;

import java.io.ByteArrayOutputStream;

public interface PercentEncoded extends CharSequence {

  default String decode() {
    return Strings.transform(
        toString(), multiplePctEncodedPattern, PercentEncoded::decodeCharacters);
  }

  @Override
  default int length() {
    return toString().length();
  }

  @Override
  default CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  @Override
  default char charAt(int index) {
    return toString().charAt(index);
  }

  private static String decodeCharacters(String percentEncodings) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    for (int i = 0; i < percentEncodings.length(); ) {
      String hexString = percentEncodings.substring(i + 1, i + 3);
      int byteValue = Integer.parseInt(hexString, 16);
      bytes.write(byteValue);
      i += 3;
    }

    return bytes.toString(UTF_8);
  }
}
