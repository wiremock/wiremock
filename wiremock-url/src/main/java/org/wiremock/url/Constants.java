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

import static java.lang.Character.toUpperCase;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

final class Constants {

  @Language("RegExp")
  static final String unreserved = "-a-zA-Z0-9\\._~";

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

  private static final boolean[] empty = new boolean[0];

  @Nullable
  static String normalise(String original, boolean[] charactersThatDoNotNeedEncoding) {
    return normalise(original, charactersThatDoNotNeedEncoding, empty);
  }

  @Nullable
  static String normalise(
      String original, boolean[] charactersThatDoNotNeedEncoding, boolean[] charactersToLeaveAsIs) {
    StringBuilder result = new StringBuilder();
    boolean changed = false;

    for (int i = 0; i < original.length(); i++) {
      char c = original.charAt(i);

      // Handle percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char maybeFirstHexDigit = original.charAt(i + 1);
        char maybeSecondHexDigit = original.charAt(i + 2);
        if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
          // Decode the percent-encoded character
          int decodedValue =
              (hexDigitToInt(maybeFirstHexDigit) << 4) | hexDigitToInt(maybeSecondHexDigit);
          char decodedChar = (char) decodedValue;

          // If the decoded character is unreserved, decode it
          if (isIn(charactersThatDoNotNeedEncoding, decodedChar)
              && !isIn(charactersToLeaveAsIs, decodedChar)) {
            result.append(decodedChar);
            changed = true;
          } else {
            // Otherwise, keep it encoded but uppercase the hex digits
            char firstHexDigitUpper = toUpperCase(maybeFirstHexDigit);
            char secondHexDigitUpper = toUpperCase(maybeSecondHexDigit);
            result.append(c).append(firstHexDigitUpper).append(secondHexDigitUpper);
            if (maybeFirstHexDigit != firstHexDigitUpper
                || maybeSecondHexDigit != secondHexDigitUpper) {
              changed = true;
            }
          }
          i += 2;
          continue;
        }
      }

      // Check if character needs encoding per WhatWG fragment percent-encode set
      if (isIn(charactersThatDoNotNeedEncoding, c)) {
        result.append(c);
      } else {
        // Encode as UTF-8 bytes
        appendPercentEncoded(c, result);
        changed = true;
      }
    }

    if (!changed) {
      return null;
    } else {
      return result.toString();
    }
  }

  @Nullable
  static String simpleNormalise(String original, boolean[] charactersThatDoNotNeedEncoding) {
    StringBuilder result = new StringBuilder();
    boolean changed = false;

    for (int i = 0; i < original.length(); i++) {
      char c = original.charAt(i);

      // Handle percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char maybeFirstHexDigit = original.charAt(i + 1);
        char maybeSecondHexDigit = original.charAt(i + 2);
        if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
          // Decode the percent-encoded character
          int decodedValue =
              (hexDigitToInt(maybeFirstHexDigit) << 4) | hexDigitToInt(maybeSecondHexDigit);
          char decodedChar = (char) decodedValue;

          // Keep it encoded but uppercase the hex digits
          char firstHexDigitUpper = toUpperCase(maybeFirstHexDigit);
          char secondHexDigitUpper = toUpperCase(maybeSecondHexDigit);
          result.append(c).append(firstHexDigitUpper).append(secondHexDigitUpper);
          if (maybeFirstHexDigit != firstHexDigitUpper
              || maybeSecondHexDigit != secondHexDigitUpper) {
            changed = true;
          }
          i += 2;
          continue;
        }
      }

      // Check if character needs encoding
      if (isIn(charactersThatDoNotNeedEncoding, c)) {
        result.append(c);
      } else {
        // Encode as UTF-8 bytes
        appendPercentEncoded(c, result);
        changed = true;
      }
    }

    if (!changed) {
      return null;
    } else {
      return result.toString();
    }
  }

  static boolean isSimpleNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    for (int i = 0; i < original.length(); i++) {
      char c = original.charAt(i);

      // Check percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char firstHexDigit = original.charAt(i + 1);
        char secondHexDigit = original.charAt(i + 2);

        if (isHexDigit(firstHexDigit) && isHexDigit(secondHexDigit)) {
          // Must be uppercase hex digits
          if (!isUpperCaseHexDigit(firstHexDigit) || !isUpperCaseHexDigit(secondHexDigit)) {
            return false;
          }

          i += 2;
          continue;
        }
      }

      if (!isIn(charactersThatDoNotNeedEncoding, c)) {
        return false;
      }
    }

    return true;
  }

  static boolean isNormalForm(
      String original, boolean[] charactersThatDoNotNeedEncoding, boolean[] charactersToLeaveAsIs) {
    for (int i = 0; i < original.length(); i++) {
      char c = original.charAt(i);

      // Check percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char firstHexDigit = original.charAt(i + 1);
        char secondHexDigit = original.charAt(i + 2);

        if (isHexDigit(firstHexDigit) && isHexDigit(secondHexDigit)) {
          // Must be uppercase hex digits
          if (!isUpperCaseHexDigit(firstHexDigit) || !isUpperCaseHexDigit(secondHexDigit)) {
            return false;
          }

          // Decode the character
          int decodedValue = (hexDigitToInt(firstHexDigit) << 4) | hexDigitToInt(secondHexDigit);
          char decodedChar = (char) decodedValue;

          // If the decoded character is unreserved, it should not be percent-encoded
          if (isIn(charactersThatDoNotNeedEncoding, decodedChar)
              && !isIn(charactersToLeaveAsIs, decodedChar)) {
            return false;
          }

          i += 2;
          continue;
        }
      }

      if (!isIn(charactersThatDoNotNeedEncoding, c) && !isIn(charactersToLeaveAsIs, c)) {
        return false;
      }
    }

    return true;
  }

  static boolean isNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    return isNormalForm(original, charactersThatDoNotNeedEncoding, empty);
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private static boolean isUpperCaseHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
  }

  private static int hexDigitToInt(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    } else if (c >= 'A' && c <= 'F') {
      return c - 'A' + 10;
    } else if (c >= 'a' && c <= 'f') {
      return c - 'a' + 10;
    }
    throw new IllegalArgumentException("Invalid hex digit: " + c);
  }

  static String encode(String unencoded, boolean[] charactersThatDoNotNeedEncoding) {
    StringBuilder result = new StringBuilder();
    var unencodedChats = unencoded.toCharArray();
    for (char c : unencodedChats) {
      if (isIn(charactersThatDoNotNeedEncoding, c)) {
        result.append(c);
      } else {
        appendPercentEncoded(c, result);
      }
    }
    return result.toString();
  }

  private static void appendPercentEncoded(char c, StringBuilder result) {
    byte[] bytes = String.valueOf(c).getBytes(UTF_8);
    for (byte b : bytes) {
      result.append('%');
      result.append(String.format("%02X", b & 0xFF));
    }
  }

  private static boolean isIn(boolean[] characterSet, char character) {
    return character < characterSet.length && characterSet[character];
  }

  private Constants() {
    throw new UnsupportedOperationException("Not instantiable");
  }
}
