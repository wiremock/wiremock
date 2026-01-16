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

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

abstract class AbstractUriMutator<SELF extends Uri.Mutator> implements Uri.Mutator {

  protected @Nullable Scheme scheme = null;
  protected @Nullable UserInfo userInfo = null;
  protected @Nullable Port port = null;
  protected @Nullable Authority authority = null;
  protected Path path = Path.ROOT;
  protected @Nullable Query query = null;
  protected @Nullable Fragment fragment = null;

  AbstractUriMutator() {}

  AbstractUriMutator(Uri uri) {
    this.scheme = uri.getScheme();
    this.authority = uri.getAuthority();
    this.path = uri.getPath();
    this.query = uri.getQuery();
    this.fragment = uri.getFragment();
  }

  protected SELF doSetScheme(@Nullable Scheme scheme) {
    this.scheme = scheme;
    return getSelf();
  }

  protected SELF doSetAuthority(@Nullable Authority authority) {
    this.authority = authority;
    this.userInfo = null;
    this.port = null;
    return getSelf();
  }

  @Override
  public @NonNull SELF setUserInfo(@Nullable UserInfo userInfo) {
    if (this.authority == null) {
      this.userInfo = userInfo;
      return getSelf();
    } else {
      return doSetAuthority(Authority.of(userInfo, authority.getHost(), authority.getPort()));
    }
  }

  @Override
  public @NonNull SELF setHost(Host host) {
    if (this.authority == null) {
      return doSetAuthority(Authority.of(userInfo, host, port));
    } else {
      return doSetAuthority(Authority.of(authority.getUserInfo(), host, authority.getPort()));
    }
  }

  @Override
  public @NonNull SELF setPort(@Nullable Port port) {
    if (this.authority == null) {
      this.port = port;
      return getSelf();
    } else {
      return doSetAuthority(Authority.of(authority.getUserInfo(), authority.getHost(), port));
    }
  }

  @Override
  public @NonNull SELF setPath(Path path) {
    this.path = requireNonNull(path);
    return getSelf();
  }

  @Override
  public @NonNull SELF setQuery(@Nullable Query query) {
    this.query = query;
    return getSelf();
  }

  @Override
  public @NonNull SELF setFragment(@Nullable Fragment fragment) {
    this.fragment = fragment;
    return getSelf();
  }

  @SuppressWarnings("unchecked")
  private SELF getSelf() {
    return (SELF) this;
  }
}
