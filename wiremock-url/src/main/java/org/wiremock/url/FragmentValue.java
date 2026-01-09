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

import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class FragmentValue implements Fragment {

  private final String fragment;
  private @Nullable volatile Boolean normalForm;

  FragmentValue(String fragment) {
    this(fragment, null);
  }

  FragmentValue(String fragment, @Nullable Boolean normalForm) {
    this.fragment = fragment;
    this.normalForm = normalForm;
  }

  @Override
  public String toString() {
    return fragment;
  }

  @Override
  public Fragment normalise() {
    if (Boolean.TRUE.equals(normalForm)) {
      return this;
    }

    String result = Constants.normalise(fragment, FragmentParser.fragmentCharSet);

    if (result == null) {
      this.normalForm = true;
      return this;
    } else {
      this.normalForm = false;
      return new FragmentValue(result, true);
    }
  }

  @Override
  public boolean isNormalForm() {
    if (normalForm == null) {
      normalForm = Constants.isNormalForm(fragment, FragmentParser.fragmentCharSet);
    }
    return normalForm;
  }

  public String fragment() {
    return fragment;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Fragment that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(fragment);
  }
}
