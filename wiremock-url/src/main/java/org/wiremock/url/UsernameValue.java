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

final class UsernameValue implements Username {

  private final String username;
  private @Nullable volatile Boolean normalForm;

  UsernameValue(String username) {
    this(username, null);
  }

  UsernameValue(String username, @Nullable Boolean normalForm) {
    this.username = username;
    this.normalForm = normalForm;
  }

  @Override
  public Username normalise() {
    if (Boolean.TRUE.equals(normalForm)) {
      return this;
    }

    String result = Constants.normalise(username, UserInfoParser.usernameCharSet);

    if (result == null) {
      this.normalForm = true;
      return this;
    } else {
      this.normalForm = false;
      return new UsernameValue(result, true);
    }
  }

  @Override
  public boolean isNormalForm() {
    if (normalForm == null) {
      normalForm = Constants.isNormalForm(username, UserInfoParser.usernameCharSet);
    }
    return normalForm;
  }

  @Override
  public String toString() {
    return username;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Username that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
