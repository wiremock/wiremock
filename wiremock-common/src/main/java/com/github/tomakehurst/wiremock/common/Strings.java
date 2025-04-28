/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static java.lang.Math.max;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

  private Strings() {}

  private static ThreadLocalRandom random() {
    return ThreadLocalRandom.current();
  }

  public static int getLevenshteinDistance(CharSequence s, CharSequence t) {
    if (s == null || t == null) {
      throw new IllegalArgumentException("Strings must not be null");
    }

    int n = s.length();
    int m = t.length();

    if (n == 0) {
      return m;
    }
    if (m == 0) {
      return n;
    }

    if (n > m) {
      // swap the input strings to consume less memory
      final CharSequence tmp = s;
      s = t;
      t = tmp;
      n = m;
      m = t.length();
    }

    final int[] p = new int[n + 1];
    // indexes into strings s and t
    int i; // iterates through s
    int j; // iterates through t
    int upperleft;
    int upper;

    char jOfT; // jth character of t
    int cost;

    for (i = 0; i <= n; i++) {
      p[i] = i;
    }

    for (j = 1; j <= m; j++) {
      upperleft = p[0];
      jOfT = t.charAt(j - 1);
      p[0] = j;

      for (i = 1; i <= n; i++) {
        upper = p[i];
        cost = s.charAt(i - 1) == jOfT ? 0 : 1;
        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
        p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperleft + cost);
        upperleft = upper;
      }
    }

    return p[n];
  }

  public static String randomAlphanumeric(final int count) {
    return random(count, true, true);
  }

  public static String random(final int count, final boolean letters, final boolean numbers) {
    return random(count, 0, 0, letters, numbers);
  }

  public static String randomAlphabetic(final int count) {
    return random(count, true, false);
  }

  public static String randomNumeric(final int count) {
    return random(count, false, true);
  }

  public static String random(final int count, final String chars) {
    if (chars == null) {
      return random(count, 0, 0, false, false, null, random());
    }
    return random(count, chars.toCharArray());
  }

  public static String random(final int count, final char... chars) {
    if (chars == null) {
      return random(count, 0, 0, false, false, null, random());
    }
    return random(count, 0, chars.length, false, false, chars, random());
  }

  public static String randomAscii(final int count) {
    return random(count, 32, 127, false, false);
  }

  public static String random(
      final int count,
      final int start,
      final int end,
      final boolean letters,
      final boolean numbers) {
    return random(count, start, end, letters, numbers, null, random());
  }

  public static String random(
      int count,
      int start,
      int end,
      final boolean letters,
      final boolean numbers,
      final char[] chars,
      final Random random) {
    if (count == 0) {
      return "";
    }
    if (count < 0) {
      throw new IllegalArgumentException(
          "Requested random string length " + count + " is less than 0.");
    }
    if (chars != null && chars.length == 0) {
      throw new IllegalArgumentException("The chars array must not be empty");
    }

    if (start == 0 && end == 0) {
      if (chars != null) {
        end = chars.length;
      } else if (!letters && !numbers) {
        end = Character.MAX_CODE_POINT;
      } else {
        end = 'z' + 1;
        start = ' ';
      }
    } else if (end <= start) {
      throw new IllegalArgumentException(
          "Parameter end (" + end + ") must be greater than start (" + start + ")");
    }

    final int zeroDigitAscii = 48;
    final int firstLetterAscii = 65;

    if (chars == null && (numbers && end <= zeroDigitAscii || letters && end <= firstLetterAscii)) {
      throw new IllegalArgumentException(
          "Parameter end ("
              + end
              + ") must be greater then ("
              + zeroDigitAscii
              + ") for generating digits "
              + "or greater then ("
              + firstLetterAscii
              + ") for generating letters.");
    }

    final StringBuilder builder = new StringBuilder(count);
    final int gap = end - start;

    while (count-- != 0) {
      final int codePoint;
      if (chars == null) {
        codePoint = random.nextInt(gap) + start;

        switch (Character.getType(codePoint)) {
          case Character.UNASSIGNED:
          case Character.PRIVATE_USE:
          case Character.SURROGATE:
            count++;
            continue;
        }

      } else {
        codePoint = chars[random.nextInt(gap) + start];
      }

      final int numberOfChars = Character.charCount(codePoint);
      if (count == 0 && numberOfChars > 1) {
        count++;
        continue;
      }

      if (letters && Character.isLetter(codePoint)
          || numbers && Character.isDigit(codePoint)
          || !letters && !numbers) {
        builder.appendCodePoint(codePoint);

        if (numberOfChars == 2) {
          count--;
        }

      } else {
        count++;
      }
    }
    return builder.toString();
  }

  public static String rightPad(final String str, final int size) {
    return rightPad(str, size, ' ');
  }

  public static String rightPad(final String str, final int size, final char padChar) {
    if (str == null) {
      return null;
    }
    final int pads = size - str.length();
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (pads > 8192) {
      return rightPad(str, size, String.valueOf(padChar));
    }
    return str.concat(repeat(padChar, pads));
  }

  public static String rightPad(final String str, final int size, String padStr) {
    if (str == null) {
      return null;
    }
    if (isEmpty(padStr)) {
      padStr = " ";
    }
    final int padLen = padStr.length();
    final int strLen = str.length();
    final int pads = size - strLen;
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (padLen == 1 && pads <= 8192) {
      return rightPad(str, size, padStr.charAt(0));
    }

    if (pads == padLen) {
      return str.concat(padStr);
    }
    if (pads < padLen) {
      return str.concat(padStr.substring(0, pads));
    }
    final char[] padding = new char[pads];
    final char[] padChars = padStr.toCharArray();
    for (int i = 0; i < pads; i++) {
      padding[i] = padChars[i % padLen];
    }
    return str.concat(new String(padding));
  }

  public static String repeat(final char ch, final int repeat) {
    if (repeat <= 0) {
      return "";
    }
    final char[] buf = new char[repeat];
    Arrays.fill(buf, ch);
    return new String(buf);
  }

  public static String stringFromBytes(byte[] bytes) {
    return stringFromBytes(bytes, UTF_8);
  }

  public static String stringFromBytes(byte[] bytes, Charset charset) {
    if (bytes == null) {
      return null;
    }

    return new String(bytes, charset);
  }

  public static byte[] bytesFromString(String str) {
    return bytesFromString(str, UTF_8);
  }

  public static byte[] bytesFromString(String str, Charset charset) {
    if (str == null) {
      return null;
    }

    return str.getBytes(charset);
  }

  public static String wrapIfLongestLineExceedsLimit(String s, int maxLineLength) {
    int longestLength = findLongestLineLength(s);
    if (longestLength > maxLineLength) {
      String wrapped = wrap(s, maxLineLength, null, true);
      return wrapped.replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    return s;
  }

  public static String wrap(
      final String str,
      final int wrapLength,
      final String newLineStr,
      final boolean wrapLongWords) {
    return wrap(str, wrapLength, newLineStr, wrapLongWords, " ");
  }

  public static String wrap(
      final String str,
      int wrapLength,
      String newLineStr,
      final boolean wrapLongWords,
      String wrapOn) {
    if (str == null) {
      return null;
    }
    if (newLineStr == null) {
      newLineStr = System.lineSeparator();
    }
    if (wrapLength < 1) {
      wrapLength = 1;
    }
    if (isBlank(wrapOn)) {
      wrapOn = " ";
    }
    final Pattern patternToWrapOn = Pattern.compile(wrapOn);
    final int inputLineLength = str.length();
    int offset = 0;
    final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

    while (offset < inputLineLength) {
      int spaceToWrapAt = -1;
      Matcher matcher =
          patternToWrapOn.matcher(
              str.substring(
                  offset,
                  Math.min(
                      (int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L),
                      inputLineLength)));
      if (matcher.find()) {
        if (matcher.start() == 0) {
          offset += matcher.end();
          continue;
        }
        spaceToWrapAt = matcher.start() + offset;
      }

      // only last line without leading spaces is left
      if (inputLineLength - offset <= wrapLength) {
        break;
      }

      while (matcher.find()) {
        spaceToWrapAt = matcher.start() + offset;
      }

      if (spaceToWrapAt >= offset) {
        // normal case
        wrappedLine.append(str, offset, spaceToWrapAt);
        wrappedLine.append(newLineStr);
        offset = spaceToWrapAt + 1;

      } else // really long word or URL
      if (wrapLongWords) {
        // wrap really long word one line at a time
        wrappedLine.append(str, offset, wrapLength + offset);
        wrappedLine.append(newLineStr);
        offset += wrapLength;
      } else {
        // do not wrap really long word, just extend beyond limit
        matcher = patternToWrapOn.matcher(str.substring(offset + wrapLength));
        if (matcher.find()) {
          spaceToWrapAt = matcher.start() + offset + wrapLength;
        }

        if (spaceToWrapAt >= 0) {
          wrappedLine.append(str, offset, spaceToWrapAt);
          wrappedLine.append(newLineStr);
          offset = spaceToWrapAt + 1;
        } else {
          wrappedLine.append(str, offset, str.length());
          offset = inputLineLength;
        }
      }
    }

    // Whatever is left in line is short enough to just pass through
    wrappedLine.append(str, offset, str.length());

    return wrappedLine.toString();
  }

  public static String substringAfterLast(final String str, final int separator) {
    if (isEmpty(str)) {
      return str;
    }
    final int pos = str.lastIndexOf(separator);
    if (pos == -1 || pos == str.length() - 1) {
      return "";
    }
    return str.substring(pos + 1);
  }

  public static String substringAfterLast(final String str, final String separator) {
    if (isEmpty(str)) {
      return str;
    }
    if (isEmpty(separator)) {
      return "";
    }
    final int pos = str.lastIndexOf(separator);
    if (pos == -1 || pos == str.length() - separator.length()) {
      return "";
    }
    return str.substring(pos + separator.length());
  }

  public static int countMatches(final CharSequence str, final char ch) {
    if (isEmpty(str)) {
      return 0;
    }
    int count = 0;
    // We could also call str.toCharArray() for faster lookups but that would generate more garbage.
    for (int i = 0; i < str.length(); i++) {
      if (ch == str.charAt(i)) {
        count++;
      }
    }
    return count;
  }

  public static int ordinalIndexOf(
      final CharSequence str, final CharSequence searchStr, final int ordinal) {
    return ordinalIndexOf(str, searchStr, ordinal, false);
  }

  private static int ordinalIndexOf(
      final CharSequence str,
      final CharSequence searchStr,
      final int ordinal,
      final boolean lastIndex) {
    if (str == null || searchStr == null || ordinal <= 0) {
      return -1;
    }
    if (searchStr.length() == 0) {
      return lastIndex ? str.length() : 0;
    }
    int found = 0;
    // set the initial index beyond the end of the string
    // this is to allow for the initial index decrement/increment
    int index = lastIndex ? str.length() : -1;
    do {
      if (lastIndex) {
        index = lastIndexOf(str, searchStr, index - 1); // step backwards through string
      } else {
        index = indexOf(str, searchStr, index + 1); // step forwards through string
      }
      if (index < 0) {
        return index;
      }
      found++;
    } while (found < ordinal);
    return index;
  }

  private static int findLongestLineLength(String s) {
    String[] lines = s.split("\n");
    int longestLength = 0;
    for (String line : lines) {
      int length = line.length();
      if (length > longestLength) {
        longestLength = length;
      }
    }

    return longestLength;
  }

  public static double normalisedLevenshteinDistance(String one, String two) {
    if (one == null || two == null) {
      return 1.0;
    }

    double maxDistance = max(one.length(), two.length());
    double actualDistance = getLevenshteinDistance(one, two);
    return (actualDistance / maxDistance);
  }

  public static String normaliseLineBreaks(String s) {
    return s.replace("\r\n", "\n").replace("\n", lineSeparator());
  }

  public static boolean isNullOrEmpty(String s) {
    return isNull(s) || s.isEmpty();
  }

  public static boolean isNotNullOrEmpty(String s) {
    return !isNullOrEmpty(s);
  }

  public static boolean isBlank(String s) {
    return isNull(s) || s.isBlank();
  }

  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  public static boolean isNull(String s) {
    return s == null;
  }

  public static boolean isNotNull(String s) {
    return !isNull(s);
  }

  public static boolean isEmpty(CharSequence charSequence) {
    return charSequence == null || charSequence.length() == 0;
  }

  public static boolean isEmpty(String s) {
    return isNull(s) || s.isEmpty();
  }

  public static boolean isNotEmpty(String s) {
    return !isEmpty(s);
  }

  public static String removeStart(String str, String remove) {
    if (isEmpty(str) || isEmpty(remove)) {
      return str;
    }
    if (str.startsWith(remove)) {
      return str.substring(remove.length());
    }
    return str;
  }

  private static boolean checkLaterThan1(
      final CharSequence cs, final CharSequence searchChar, final int len2, final int start1) {
    for (int i = 1, j = len2 - 1; i <= j; i++, j--) {
      if (cs.charAt(start1 + i) != searchChar.charAt(i)
          || cs.charAt(start1 + j) != searchChar.charAt(j)) {
        return false;
      }
    }
    return true;
  }

  private static int indexOf(
      final CharSequence cs, final CharSequence searchChar, final int start) {
    if (cs instanceof String) {
      return ((String) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuilder) {
      return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuffer) {
      return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
    }
    return cs.toString().indexOf(searchChar.toString(), start);
  }

  private static int lastIndexOf(final CharSequence cs, final CharSequence searchChar, int start) {
    if (searchChar == null || cs == null) {
      return -1;
    }
    if (searchChar instanceof String) {
      if (cs instanceof String) {
        return ((String) cs).lastIndexOf((String) searchChar, start);
      }
      if (cs instanceof StringBuilder) {
        return ((StringBuilder) cs).lastIndexOf((String) searchChar, start);
      }
      if (cs instanceof StringBuffer) {
        return ((StringBuffer) cs).lastIndexOf((String) searchChar, start);
      }
    }

    final int len1 = cs.length();
    final int len2 = searchChar.length();

    if (start > len1) {
      start = len1;
    }

    if (start < 0 || len2 > len1) {
      return -1;
    }

    if (len2 == 0) {
      return start;
    }

    if (len2 <= 16) {
      if (cs instanceof String) {
        return ((String) cs).lastIndexOf(searchChar.toString(), start);
      }
      if (cs instanceof StringBuilder) {
        return ((StringBuilder) cs).lastIndexOf(searchChar.toString(), start);
      }
      if (cs instanceof StringBuffer) {
        return ((StringBuffer) cs).lastIndexOf(searchChar.toString(), start);
      }
    }

    if (start + len2 > len1) {
      start = len1 - len2;
    }

    final char char0 = searchChar.charAt(0);

    int i = start;
    while (true) {
      while (cs.charAt(i) != char0) {
        i--;
        if (i < 0) {
          return -1;
        }
      }
      if (checkLaterThan1(cs, searchChar, len2, i)) {
        return i;
      }
      i--;
      if (i < 0) {
        return -1;
      }
    }
  }
}
