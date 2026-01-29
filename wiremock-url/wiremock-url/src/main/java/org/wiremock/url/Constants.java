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

import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;

final class Constants {

  @Language("RegExp")
  static final String unreserved = "-a-zA-Z0-9\\._~";

  static final boolean[] empty = new boolean[0];

  static final boolean[] unreservedCharSet =
      combine(
          includeRange('a', 'z'),
          includeRange('A', 'Z'),
          includeRange('0', '9'),
          include('-', '.', '_', '~'));

  @Language("RegExp")
  static final String pctEncoded = "%[0-9a-fA-F]{2}";

  static final Pattern pctEncodedPattern = Pattern.compile(pctEncoded);
  static final Pattern multiplePctEncodedPattern = Pattern.compile("(?:" + pctEncoded + ")+");

  @Language("RegExp")
  static final String subDelims = "!\\$&'\\(\\)\\*\\+,;=";

  static final boolean[] subDelimCharSet =
      include('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=');

  @Language("RegExp")
  static final String alwaysIllegal =
      "\\u0000-\\u0008\\u000A-\\u001F\\u007F\\u0080-\\u009F\\uD800-\\uDFFF";

  static final boolean[] pcharCharSet =
      combine(unreservedCharSet, subDelimCharSet, include(':', '@'));

  static boolean[] combine(boolean[] one, boolean[]... charSets) {
    int length = one.length;
    for (boolean[] charSet : charSets) {
      length = Math.max(length, charSet.length);
    }
    boolean[] result = new boolean[length];
    System.arraycopy(one, 0, result, 0, one.length);
    for (boolean[] charSet : charSets) {
      for (int i = 0; i < charSet.length; i++) {
        result[i] = result[i] || charSet[i];
      }
    }
    return result;
  }

  static boolean[] include(char... chars) {
    boolean[] charSet = new boolean[128];
    for (char aChar : chars) {
      charSet[aChar] = true;
    }
    return charSet;
  }

  static boolean[] includeRange(char start, char end) {
    boolean[] charSet = new boolean[128];
    for (int i = start; i <= end; i++) {
      charSet[i] = true;
    }
    return charSet;
  }

  static boolean[] remove(boolean[] original, char... toRemove) {
    boolean[] result = new boolean[original.length];
    System.arraycopy(original, 0, result, 0, original.length);
    for (char c : toRemove) {
      result[c] = false;
    }
    return result;
  }

  private Constants() {
    throw new UnsupportedOperationException("Not instantiable");
  }
}
