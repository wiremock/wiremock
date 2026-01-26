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

final class UserInfoValue implements UserInfo {

  private final String userInfo;
  private final Username username;
  private final @Nullable Password password;

  UserInfoValue(String userInfo) {
    this.userInfo = userInfo;
    var components = userInfo.split(":", 2);
    this.username = new UsernameValue(components[0]);
    if (components.length == 2) {
      this.password = new PasswordValue(components[1]);
    } else {
      this.password = null;
    }
  }

  UserInfoValue(Username username, @Nullable Password password) {
    this.userInfo = password == null ? username.toString() : username + ":" + password;
    this.username = username;
    this.password = password;
  }

  @Override
  public String toString() {
    return userInfo;
  }

  @Override
  public Username getUsername() {
    return username;
  }

  @Override
  public @Nullable Password getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof UserInfo that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(userInfo);
  }

  @Override
  public UserInfo normalise() {

    var normalisedUsername = username.normalise();
    var normalisedPassword = password != null ? password.normalise() : null;

    if (normalisedUsername.equals(username) && Objects.equals(normalisedPassword, password)) {
      return this;
    } else {
      return new UserInfoValue(normalisedUsername, normalisedPassword);
    }
  }

  @Override
  public boolean isNormalForm() {
    return username.isNormalForm() && (password == null || password.isNormalForm());
  }
}
