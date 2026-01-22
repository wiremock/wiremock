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

import static org.wiremock.url.Constants.*;

public final class FragmentParser implements PercentEncodedStringParser<Fragment> {

  public static final FragmentParser INSTANCE = new FragmentParser();

  @Override
  public Class<Fragment> getType() {
    return Fragment.class;
  }

  @Override
  public Fragment parse(String stringForm) {
    if (stringForm.isEmpty()) {
      return Fragment.EMPTY;
    } else {
      return new FragmentValue(stringForm);
    }
  }

  static final boolean[] fragmentCharSet = combine(pcharCharSet, include('/', '?'));

  @Override
  public Fragment encode(String unencoded) {
    if (unencoded.isEmpty()) {
      return Fragment.EMPTY;
    } else {
      var result = Constants.encode(unencoded, fragmentCharSet);
      return new FragmentValue(result, true);
    }
  }
}
