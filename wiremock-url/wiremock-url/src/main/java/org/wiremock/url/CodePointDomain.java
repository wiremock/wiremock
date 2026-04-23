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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;

sealed interface CodePointOrHexCodePoint extends AppendableTo {
  CodePoint decode();

  boolean isEncoded();
}

sealed interface HexCodePoint extends CodePointOrHexCodePoint {

  @Override
  CodePoint decode();

  @Override
  default boolean isEncoded() {
    return true;
  }

  HexCodePoint toUpperCase();

  boolean isUpperCase();

  static int hexDigitToInt(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    } else if (c >= 'A' && c <= 'F') {
      return c - 'A' + 10;
    } else if (c >= 'a' && c <= 'f') {
      return c - 'a' + 10;
    }
    throw new IllegalArgumentException("Invalid hex digit: " + c);
  }
}

record CodePoint(int codePoint) implements CodePointOrHexCodePoint {

  @Override
  public boolean isEncoded() {
    return false;
  }

  public CodePointOrHexCodePoint maybePercentEncode(boolean[] charactersThatDoNotNeedEncoding) {
    if (isIn(charactersThatDoNotNeedEncoding)) {
      return this;
    } else {
      return percentEncode();
    }
  }

  private HexCodePoint percentEncode() {
    int codePoint = codePoint();
    String str = new String(Character.toChars(codePoint));
    byte[] bytes = str.getBytes(UTF_8);
    HexCharacter[] hexCharacters = new HexCharacter[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      hexCharacters[i] = getHexCharacter(bytes[i]);
    }
    return new HexSequence(hexCharacters);
  }

  private static HexCharacter getHexCharacter(byte aByte) {
    int byteValue = aByte & 0xFF;
    return new HexCharacter(toHexDigit(byteValue >> 4), toHexDigit(byteValue & 0x0F));
  }

  static char toHexDigit(int value) {
    return (char) (value < 10 ? '0' + value : 'A' + value - 10);
  }

  public boolean isIn(boolean[] charactersThatDoNotNeedEncoding) {
    return codePoint < charactersThatDoNotNeedEncoding.length
        && charactersThatDoNotNeedEncoding[codePoint];
  }

  @Override
  public String toString() {
    return new String(Character.toChars(codePoint));
  }

  @Override
  public CodePoint decode() {
    return this;
  }

  @Override
  public void appendTo(StringBuilder builder) {
    builder.appendCodePoint(codePoint);
  }
}

final class HexSequence implements HexCodePoint {

  private final HexCharacter[] hexChars;

  HexSequence(HexCharacter[] hexChars) {
    this.hexChars = hexChars;
    if (hexChars.length == 0 || hexChars.length > 4) {
      throw new IllegalArgumentException(
          "hex sequence must be 1-4 hex characters, was " + Arrays.toString(hexChars));
    }
  }

  @Override
  public CodePoint decode() {
    byte[] bytes = new byte[hexChars.length];
    for (int i = 0; i < hexChars.length; i++) {
      HexCharacter hc = hexChars[i];
      bytes[i] =
          (byte)
              ((HexCodePoint.hexDigitToInt(hc.digit1()) << 4)
                  | HexCodePoint.hexDigitToInt(hc.digit2()));
    }

    // Convert UTF-8 bytes to String, then get the code point
    String decoded = new String(bytes, UTF_8);
    int codePoint = decoded.codePointAt(0);
    return new CodePoint(codePoint);
  }

  @Override
  public HexSequence toUpperCase() {
    HexCharacter[] upper = new HexCharacter[hexChars.length];
    boolean changed = false;
    for (int i = 0; i < hexChars.length; i++) {
      var original = hexChars[i];
      var upperCase = original.toUpperCase();
      if (!upperCase.equals(original)) {
        changed = true;
      }
      upper[i] = upperCase;
    }
    if (changed) {
      return new HexSequence(upper);
    } else {
      return this;
    }
  }

  @Override
  public boolean isUpperCase() {
    for (HexCharacter hexChar : hexChars) {
      if (!hexChar.isUpperCase()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (HexCharacter hexChar : hexChars) {
      sb.append(hexChar);
    }
    return sb.toString();
  }

  @Override
  public void appendTo(StringBuilder builder) {
    for (HexCharacter hexChar : hexChars) {
      hexChar.appendTo(builder);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HexSequence that)) {
      return false;
    }
    return Arrays.equals(this.hexChars, that.hexChars);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(hexChars);
  }
}

record HexCharacter(char digit1, char digit2) implements HexCodePoint {
  @Override
  public CodePoint decode() {
    return new CodePoint(
        (char) ((HexCodePoint.hexDigitToInt(digit1) << 4) | HexCodePoint.hexDigitToInt(digit2)));
  }

  @Override
  public HexCharacter toUpperCase() {
    var char1Upper = Character.toUpperCase(digit1);
    var char2Upper = Character.toUpperCase(digit2);
    if (char1Upper == digit1 && char2Upper == digit2) {
      return this;
    } else {
      return new HexCharacter(char1Upper, char2Upper);
    }
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  @Override
  public boolean isUpperCase() {
    return isUpperCaseHexDigit(digit1) && isUpperCaseHexDigit(digit2);
  }

  @Override
  public String toString() {
    return new String(new char[] {'%', digit1, digit2});
  }

  @Override
  public void appendTo(StringBuilder builder) {
    builder.append('%').append(digit1).append(digit2);
  }

  private static boolean isUpperCaseHexDigit(char c) {
    return Character.isUpperCase(c) || Character.isDigit(c);
  }
}
