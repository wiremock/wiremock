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

public interface Fragment extends PctEncoded {

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
  }
}
