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

import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface Fragment extends PercentEncoded {

  Fragment normalise();

  static Fragment parse(CharSequence fragment) throws IllegalFragment {
    return FragmentParser.INSTANCE.parse(fragment);
  }

  static Fragment encode(String unencoded) {
    return FragmentParser.INSTANCE.encode(unencoded);
  }
}

class FragmentParser implements PercentEncodedCharSequenceParser<Fragment> {

  static final FragmentParser INSTANCE = new FragmentParser();

  @Override
  public Fragment parse(CharSequence stringForm) {
    return new Fragment(stringForm.toString());
  }

  @Override
  public Fragment encode(String unencoded) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < unencoded.length(); i++) {
      char c = unencoded.charAt(i);
      if (isUnreserved(c) || isSubDelim(c) || c == ':' || c == '@' || c == '/' || c == '?') {
        result.append(c);
      } else {
        byte[] bytes = String.valueOf(c).getBytes(UTF_8);
        for (byte b : bytes) {
          result.append('%');
          result.append(String.format("%02X", b & 0xFF));
        }
      }
    }
    return new Fragment(result.toString());
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

  record Fragment(String fragment) implements org.wiremock.url.Fragment {

    @Override
    public String toString() {
      return fragment;
    }

    private static final boolean[] unreserved = combine(
        includeRange('a', 'z'),
        includeRange('A', 'Z'),
        includeRange('0', '9'),
        include('-', '.', '_', '~')
    );

    private static final boolean[] subDelimCharSet = include('!', '\\', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=');

    private static final boolean[] pcharCharset = combine(
        unreserved,
        subDelimCharSet,
        include(':', '@')
    );

    private static final boolean[] fragmentCharSet = combine(
        pcharCharset,
        include('/', '?')
    );

    private static boolean[] combine(boolean[] one, boolean[]... charSets) {
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

    private static boolean[] include(String chars) {
      return include(new boolean[128], chars.toCharArray());
    }

    private static boolean[] include(char... chars) {
      return include(new boolean[128], chars);
    }

    private static boolean[] include(boolean[] charSet, String chars) {
      return include(charSet, chars.toCharArray());
    }

    private static boolean[] include(boolean[] charSet, char[] chars) {
      for (char aChar : chars) {
        charSet[aChar] = true;
      }
      return charSet;
    }

    private static boolean[] includeRange(char start, char end) {
      return include(new boolean[128], start, end);
    }

    private static boolean[] include(boolean[] charSet, char start, char end) {
      for (int i = start; i <= end; i++) {
        charSet[i] = true;
      }
      return charSet;
    }

    @Override
    public org.wiremock.url.Fragment normalise() {
      StringBuilder result = new StringBuilder();
      boolean changed = false;

      for (int i = 0; i < fragment.length(); i++) {
        char c = fragment.charAt(i);

        // Preserve already percent-encoded sequences
        if (c == '%'
            && i + 2 < fragment.length()
            && isHexDigit(fragment.charAt(i + 1))
            && isHexDigit(fragment.charAt(i + 2))) {
          result.append(c).append(fragment.charAt(i + 1)).append(fragment.charAt(i + 2));
          i += 2;
          continue;
        }

        // Check if character needs encoding per WhatWG fragment percent-encode set
        if (shouldPercentEncodeInFragment(c)) {
          // Encode as UTF-8 bytes
          byte[] bytes = String.valueOf(c).getBytes(UTF_8);
          for (byte b : bytes) {
            result.append('%');
            result.append(String.format("%02X", b & 0xFF));
          }
          changed = true;
        } else {
          result.append(c);
        }
      }

      if (!changed) {
        return this;
      } else {
        return new Fragment(result.toString());
      }
    }

    private boolean isHexDigit(char c) {
      return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private boolean shouldPercentEncodeInFragment(char c) {
      return c >= fragmentCharSet.length || !fragmentCharSet[c];
    }
  }
}
