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

final class PasswordValue implements Password {

  private final String password;

  PasswordValue(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return password;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Password that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(password);
  }
}
