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

import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.pcharCharSet;

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

  private static final boolean[] fragmentCharSet = combine(pcharCharSet, include('/', '?'));

  @Override
  public Fragment encode(String unencoded) {
    var result = Constants.encode(unencoded, fragmentCharSet);
    return new Fragment(result);
  }

  record Fragment(String fragment) implements org.wiremock.url.Fragment {

    @Override
    public String toString() {
      return fragment;
    }

    @Override
    public org.wiremock.url.Fragment normalise() {

      String result = Constants.normalise(fragment, fragmentCharSet);

      if (result == null) {
        return this;
      } else {
        return new Fragment(result);
      }
    }
  }
}
