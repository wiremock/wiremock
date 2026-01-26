/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
  private final MemoisedNormalisable<Fragment> memoisedNormalisable;

  FragmentValue(String fragment) {
    this(fragment, null);
  }

  FragmentValue(String fragment, @Nullable Boolean isNormalForm) {
    this.fragment = fragment;
    this.memoisedNormalisable =
        new MemoisedNormalisable<>(this, isNormalForm, this::isNormalFormWork, this::normaliseWork);
  }

  @Override
  public String toString() {
    return fragment;
  }

  @Override
  public Fragment normalise() {
    return memoisedNormalisable.normalise();
  }

  private @Nullable Fragment normaliseWork() {
    String result = Constants.simpleNormalise(fragment, FragmentParser.fragmentCharSet);
    return result != null ? new FragmentValue(result, true) : null;
  }

  @Override
  public boolean isNormalForm() {
    return memoisedNormalisable.isNormalForm();
  }

  private boolean isNormalFormWork() {
    return Constants.isSimpleNormalForm(fragment, FragmentParser.fragmentCharSet);
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
