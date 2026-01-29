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
    var result = new AppendableToAwareStringBuilder();
    var percentEncodedStream = new PercentEncodedStream(input);
    while (percentEncodedStream.hasNext()) {
      result.append(percentEncodedStream.next().decode());
    }
    return result.toString();
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
    var result = new AppendableToAwareStringBuilder();
    boolean changed = false;

    var percentEncodedStream = new PercentEncodedStream(original);
    while (percentEncodedStream.hasNext()) {
      var charOrHex = percentEncodedStream.next();
      if (charOrHex instanceof HexCodePoint hex) {
        var decodedChar = hex.decode();

        // If the decoded character is unreserved, decode it
        if (decodedChar.isIn(charactersThatDoNotNeedEncoding)
            && !decodedChar.isIn(charactersToLeaveAsIs)) {
          result.append(decodedChar);
          changed = true;
        } else {
          // Otherwise, keep it encoded but uppercase the hex digits
          var upperCased = hex.toUpperCase();
          result.append(upperCased);
          if (!upperCased.equals(hex)) {
            changed = true;
          }
        }
      } else if (charOrHex instanceof CodePoint codePoint) {
        var maybeEncoded = codePoint.maybePercentEncode(charactersThatDoNotNeedEncoding);
        result.append(maybeEncoded);
        changed = maybeEncoded.isEncoded() || changed;
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
    var result = new AppendableToAwareStringBuilder();
    boolean changed = false;

    var percentEncodedStream = new PercentEncodedStream(original);
    while (percentEncodedStream.hasNext()) {
      var charOrHex = percentEncodedStream.next();
      if (charOrHex instanceof HexCodePoint hex) {
        var upperCased = hex.toUpperCase();
        result.append(upperCased);
        if (!upperCased.equals(hex)) {
          changed = true;
        }
      } else if (charOrHex instanceof CodePoint codePoint) {
        var maybeEncoded = codePoint.maybePercentEncode(charactersThatDoNotNeedEncoding);
        result.append(maybeEncoded);
        changed = maybeEncoded.isEncoded() || changed;
      }
    }

    if (!changed) {
      return null;
    } else {
      return result.toString();
    }
  }

  static boolean isSimpleNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    var percentEncodedStream = new PercentEncodedStream(original);
    while (percentEncodedStream.hasNext()) {
      var charOrHex = percentEncodedStream.next();
      if (charOrHex instanceof HexCodePoint hex) {
        if (!hex.isUpperCase()) {
          return false;
        }
      } else if (charOrHex instanceof CodePoint codePoint
          && !codePoint.isIn(charactersThatDoNotNeedEncoding)) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("SameParameterValue")
  static boolean isNormalForm(
      String original, boolean[] charactersThatDoNotNeedEncoding, boolean[] charactersToLeaveAsIs) {
    var percentEncodedStream = new PercentEncodedStream(original);
    while (percentEncodedStream.hasNext()) {
      var charOrHex = percentEncodedStream.next();
      if (charOrHex instanceof HexCodePoint hex) {
        if (!hex.isUpperCase()) {
          return false;
        }

        var decodedChar = hex.decode();

        // If the decoded character is unreserved, it should not be percent-encoded
        if (decodedChar.isIn(charactersThatDoNotNeedEncoding)
            && !decodedChar.isIn(charactersToLeaveAsIs)) {
          return false;
        }
      } else if (charOrHex instanceof CodePoint character
          && !character.isIn(charactersThatDoNotNeedEncoding)
          && !character.isIn(charactersToLeaveAsIs)) {
        return false;
      }
    }

    return true;
  }

  static boolean isNormalForm(String original, boolean[] charactersThatDoNotNeedEncoding) {
    return isNormalForm(original, charactersThatDoNotNeedEncoding, Constants.empty);
  }

  static String encode(String unencoded, boolean[] charactersThatDoNotNeedEncoding) {
    var result = new AppendableToAwareStringBuilder();
    new CodePointStream(unencoded)
        .forEachRemaining(
            codePoint ->
                result.append(codePoint.maybePercentEncode(charactersThatDoNotNeedEncoding)));
    return result.toString();
  }
}
