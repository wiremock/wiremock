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

public interface Fragment extends PercentEncoded {

  Fragment normalise();

  static Fragment parse(CharSequence fragment) throws IllegalFragment {
    return FragmentParser.INSTANCE.parse(fragment);
  }
}

class FragmentParser implements CharSequenceParser<Fragment> {

  static final FragmentParser INSTANCE = new FragmentParser();

  @Override
  public Fragment parse(CharSequence stringForm) {
    return new Fragment(stringForm.toString());
  }

  record Fragment(String fragment) implements org.wiremock.url.Fragment {

    @Override
    public int length() {
      return fragment.length();
    }

    @Override
    public char charAt(int index) {
      return fragment.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return fragment.subSequence(start, end);
    }

    @Override
    public String toString() {
      return fragment;
    }

    @Override
    public String decode() {
      throw new UnsupportedOperationException();
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
      // WhatWG query percent-encode set:
      // - C0 controls (0x00-0x1F)
      // - Space (0x20)
      // - " (0x22)
      // - ` (0x23)
      // - < (0x3C)
      // - > (0x3E)
      // - Characters > 0x7E (non-ASCII)

      if (c <= 0x1F) return true; // C0 controls
      if (c == 0x20) return true; // space
      if (c == '"') return true; // 0x22
      if (c == '<') return true; // 0x3C
      if (c == '`') return true; // 0x3C
      if (c == '>') return true; // 0x3E
      return c > 0x7E; // non-ASCII
    }
  }
}
