/*
 * Copyright (C) 2026 Thomas Akehurst
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
import static org.wiremock.url.Constants.multiplePctEncodedPattern;

import java.io.ByteArrayOutputStream;
import org.jspecify.annotations.Nullable;

final class PercentEncoding {

  /**
   * Decodes all percent-encoded sequences in a string (handles mixed content).
   *
   * <p>This method can handle strings containing both encoded and unencoded characters, like {@code
   * a%C3%9Fc}. It will decode only the percent-encoded sequences and leave other characters
   * unchanged.
   *
   * @param input a string that may contain percent-encoded sequences
   * @return the decoded string
   */
  static String decode(String input) {
    return Strings.transform(input, multiplePctEncodedPattern, PercentEncoding::decodeCharacters);
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

  PercentEncoding() {
    throw new UnsupportedOperationException("not instantiable");
  }

  @Nullable
  static String normalise(String original, boolean[] charactersThatDoNotNeedEncoding) {
    return normalise(original, charactersThatDoNotNeedEncoding, Constants.empty);
  }

  @Nullable
  static String normalise(
      String original, boolean[] charactersThatDoNotNeedEncoding, boolean[] charactersToLeaveAsIs) {
    StringBuilder result = new StringBuilder();
    boolean changed = false;

    for (int i = 0; i < original.length(); ) {
      int codePoint = original.codePointAt(i);
      char c = (char) codePoint;

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
          i += 3; // Skip past %XX
          continue;
        }
      }

      // Check if character needs encoding per WhatWG fragment percent-encode set
      if (codePoint <= 0xFFFF && isIn(charactersThatDoNotNeedEncoding, c)) {
        result.appendCodePoint(codePoint);
        i += Character.charCount(codePoint);
      } else {
        // Encode as UTF-8 bytes
        appendPercentEncoded(codePoint, result);
        changed = true;
        i += Character.charCount(codePoint);
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

    for (int i = 0; i < original.length(); ) {
      int codePoint = original.codePointAt(i);
      char c = (char) codePoint;

      // Handle percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char maybeFirstHexDigit = original.charAt(i + 1);
        char maybeSecondHexDigit = original.charAt(i + 2);
        if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
          // Keep it encoded but uppercase the hex digits
          char firstHexDigitUpper = toUpperCase(maybeFirstHexDigit);
          char secondHexDigitUpper = toUpperCase(maybeSecondHexDigit);
          result.append(c).append(firstHexDigitUpper).append(secondHexDigitUpper);
          if (maybeFirstHexDigit != firstHexDigitUpper
              || maybeSecondHexDigit != secondHexDigitUpper) {
            changed = true;
          }
          i += 3; // Skip past %XX
          continue;
        }
      }

      // Check if character needs encoding
      if (codePoint <= 0xFFFF && isIn(charactersThatDoNotNeedEncoding, c)) {
        result.appendCodePoint(codePoint);
        i += Character.charCount(codePoint);
      } else {
        // Encode as UTF-8 bytes
        appendPercentEncoded(codePoint, result);
        changed = true;
        i += Character.charCount(codePoint);
      }
    }

    if (!changed) {
      return null;
    } else {
      return result.toString();
    }
  }

  static boolean isSimpleNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    for (int i = 0; i < original.length(); ) {
      int codePoint = original.codePointAt(i);
      char c = (char) codePoint;

      // Check percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char maybeFirstHexDigit = original.charAt(i + 1);
        char maybeSecondHexDigit = original.charAt(i + 2);
        if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
          // Must be uppercase hex digits
          if (!isUpperCaseHexDigit(maybeFirstHexDigit) || !isUpperCaseHexDigit(maybeSecondHexDigit)) {
            return false;
          }

          i += 3; // Skip past %XX
          continue;
        }
      }

      // Non-BMP characters (code points > 0xFFFF) must be percent-encoded
      if (codePoint > 0xFFFF || !isIn(charactersThatDoNotNeedEncoding, c)) {
        return false;
      }

      i += Character.charCount(codePoint);
    }

    return true;
  }

  static boolean isNormalForm(
      String original, boolean[] charactersThatDoNotNeedEncoding, boolean[] charactersToLeaveAsIs) {
    for (int i = 0; i < original.length(); ) {
      int codePoint = original.codePointAt(i);
      char c = (char) codePoint;

      // Check percent-encoded sequences
      if (c == '%' && i + 2 < original.length()) {
        char maybeFirstHexDigit = original.charAt(i + 1);
        char maybeSecondHexDigit = original.charAt(i + 2);
        if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
          // Must be uppercase hex digits
          if (!isUpperCaseHexDigit(maybeFirstHexDigit) || !isUpperCaseHexDigit(maybeSecondHexDigit)) {
            return false;
          }

          // Decode the character
          int decodedValue = (hexDigitToInt(maybeFirstHexDigit) << 4) | hexDigitToInt(maybeSecondHexDigit);
          char decodedChar = (char) decodedValue;

          // If the decoded character is unreserved, it should not be percent-encoded
          if (isIn(charactersThatDoNotNeedEncoding, decodedChar)
              && !isIn(charactersToLeaveAsIs, decodedChar)) {
            return false;
          }

          i += 3; // Skip past %XX
          continue;
        }
      }

      // Non-BMP characters (code points > 0xFFFF) must be percent-encoded
      if (codePoint > 0xFFFF
          || (!isIn(charactersThatDoNotNeedEncoding, c) && !isIn(charactersToLeaveAsIs, c))) {
        return false;
      }

      i += Character.charCount(codePoint);
    }

    return true;
  }

  static boolean isNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    return isNormalForm(original, charactersThatDoNotNeedEncoding, Constants.empty);
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
    for (int i = 0; i < unencoded.length(); ) {
      int codePoint = unencoded.codePointAt(i);
      // For BMP characters (codePoint < 0x10000), check if they need encoding
      if (codePoint <= 0xFFFF && isIn(charactersThatDoNotNeedEncoding, (char) codePoint)) {
        result.appendCodePoint(codePoint);
      } else {
        appendPercentEncoded(codePoint, result);
      }
      i += Character.charCount(codePoint);
    }
    return result.toString();
  }

  private static void appendPercentEncoded(int codePoint, StringBuilder result) {
    String str = new String(Character.toChars(codePoint));
    byte[] bytes = str.getBytes(UTF_8);
    for (byte b : bytes) {
      result.append('%');
      result.append(String.format("%02X", b & 0xFF));
    }
  }

  private static boolean isIn(boolean[] characterSet, char character) {
    return character < characterSet.length && characterSet[character];
  }
}
