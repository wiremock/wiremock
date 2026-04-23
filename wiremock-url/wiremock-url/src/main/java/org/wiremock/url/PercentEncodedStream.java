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

class PercentEncodedStream extends StringTokenStream<CodePointOrHexCodePoint> {

  PercentEncodedStream(String input) {
    super(input);
  }

  @Override
  public CodePointOrHexCodePoint next() {

    // Check percent-encoded sequences
    var maybeHex = nextHex(index);
    if (maybeHex != null) {
      // Decode the first byte to check if it's a multi-byte UTF-8 sequence
      int decodedChar = maybeHex.decode().codePoint();
      int firstByte = decodedChar & 0xFF;
      int utf8Length = getUtf8SequenceLength(firstByte);

      if (utf8Length == 1) {
        index += 3;
        return maybeHex;
      } else {
        // Multi-byte UTF-8 sequence - read the continuation bytes
        HexCharacter[] hexChars = new HexCharacter[utf8Length];
        hexChars[0] = maybeHex;

        for (int i = 1; i < utf8Length; i++) {
          var continuationHex = nextHex(index + (i * 3));
          if (continuationHex == null) {
            // Invalid UTF-8 sequence - treat first byte as standalone
            index += 3;
            return maybeHex;
          }
          hexChars[i] = continuationHex;
        }

        index += utf8Length * 3;
        return new HexSequence(hexChars);
      }
    }
    int codePoint = input.codePointAt(index);
    index += Character.charCount(codePoint);
    return new CodePoint(codePoint);
  }

  private static int getUtf8SequenceLength(int firstByte) {
    // Check the high bits to determine UTF-8 sequence length
    if ((firstByte & 0x80) == 0) {
      // 0xxxxxxx = 1 byte (ASCII)
      return 1;
    } else if ((firstByte & 0xE0) == 0xC0) {
      // 110xxxxx = 2 bytes
      return 2;
    } else if ((firstByte & 0xF0) == 0xE0) {
      // 1110xxxx = 3 bytes
      return 3;
    } else if ((firstByte & 0xF8) == 0xF0) {
      // 11110xxx = 4 bytes
      return 4;
    } else {
      // Invalid UTF-8 lead byte - treat as single byte
      return 1;
    }
  }

  private @Nullable HexCharacter nextHex(int startPoint) {
    if (startPoint >= input.length()) {
      return null;
    }
    var startChar = input.charAt(startPoint);
    if (startChar == '%' && startPoint + 2 < input.length()) {
      char maybeFirstHexDigit = input.charAt(startPoint + 1);
      char maybeSecondHexDigit = input.charAt(startPoint + 2);
      if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
        return new HexCharacter(maybeFirstHexDigit, maybeSecondHexDigit);
      }
    }
    return null;
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
  }
}
