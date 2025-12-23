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

record FragmentValue(String fragment) implements Fragment {

  @Override
  public String toString() {
    return fragment;
  }

  @Override
  public Fragment normalise() {

    String result = Constants.normalise(fragment, FragmentParser.fragmentCharSet);

    if (result == null) {
      return this;
    } else {
      return new FragmentValue(result);
    }
  }
}
