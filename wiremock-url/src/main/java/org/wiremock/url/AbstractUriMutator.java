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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"NullableProblems", "unchecked"})
abstract class AbstractUriMutator<@NonNull SELF extends Uri.Mutator> implements Uri.Mutator {

  @Nullable protected Scheme scheme = null;
  @Nullable protected UserInfo userInfo = null;
  @Nullable protected Port port = null;
  @Nullable protected Authority authority = null;
  protected Path path = Path.ROOT;
  @Nullable protected Query query = null;
  @Nullable protected Fragment fragment = null;

  AbstractUriMutator() {}

  AbstractUriMutator(Uri uri) {
    this.scheme = uri.getScheme();
    this.authority = uri.getAuthority();
    this.path = uri.getPath();
    this.query = uri.getQuery();
    this.fragment = uri.getFragment();
  }

  public SELF setScheme(@Nullable Scheme scheme) {
    this.scheme = scheme;
    return (SELF) this;
  }

  public SELF setAuthority(@Nullable Authority authority) {
    this.authority = authority;
    this.userInfo = null;
    this.port = null;
    return (SELF) this;
  }

  public SELF setUserInfo(@Nullable UserInfo userInfo) {
    if (this.authority == null) {
      this.userInfo = userInfo;
      return (SELF) this;
    } else {
      return setAuthority(Authority.of(userInfo, authority.getHost(), authority.getPort()));
    }
  }

  public SELF setHost(Host host) {
    if (this.authority == null) {
      return setAuthority(Authority.of(userInfo, host, port));
    } else {
      return setAuthority(Authority.of(authority.getUserInfo(), host, authority.getPort()));
    }
  }

  public SELF setPort(@Nullable Port port) {
    if (this.authority == null) {
      this.port = port;
      return (SELF) this;
    } else {
      return setAuthority(Authority.of(authority.getUserInfo(), authority.getHost(), port));
    }
  }

  public SELF setPath(Path path) {
    this.path = path;
    return (SELF) this;
  }

  public SELF setQuery(@Nullable Query query) {
    this.query = query;
    return (SELF) this;
  }

  public SELF setFragment(@Nullable Fragment fragment) {
    this.fragment = fragment;
    return (SELF) this;
  }
}
