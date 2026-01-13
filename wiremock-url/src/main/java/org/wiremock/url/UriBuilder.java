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

import static org.wiremock.url.AbsoluteUriBuilder.buildUri;

import org.jspecify.annotations.Nullable;
import org.wiremock.url.Uri.Builder;

class UriBuilder implements Builder {

  @Nullable private Scheme scheme = null;
  @Nullable private UserInfo userInfo = null;
  @Nullable private Port port = null;
  @Nullable private Authority authority = null;
  private Path path = Path.ROOT;
  @Nullable Query query = null;
  @Nullable Fragment fragment = null;

  UriBuilder() {}

  UriBuilder(Uri uri) {
    this.scheme = uri.getScheme();
    this.authority = uri.getAuthority();
    this.path = uri.getPath();
    this.query = uri.getQuery();
    this.fragment = uri.getFragment();
  }

  @Override
  public UriBuilder setScheme(@Nullable Scheme scheme) {
    this.scheme = scheme;
    return this;
  }

  @Override
  public UriBuilder setAuthority(@Nullable Authority authority) {
    this.authority = authority;
    this.userInfo = null;
    this.port = null;
    return this;
  }

  @Override
  public UriBuilder setUserInfo(@Nullable UserInfo userInfo) {
    if (this.authority == null) {
      this.userInfo = userInfo;
    } else {
      setAuthority(Authority.of(userInfo, authority.getHost(), authority.getPort()));
    }
    return this;
  }

  @Override
  public UriBuilder setHost(Host host) {
    if (this.authority == null) {
      setAuthority(Authority.of(userInfo, host, port));
    } else {
      setAuthority(Authority.of(authority.getUserInfo(), host, authority.getPort()));
    }
    return this;
  }

  @Override
  public UriBuilder setPort(@Nullable Port port) {
    if (this.authority == null) {
      this.port = port;
    } else {
      setAuthority(Authority.of(authority.getUserInfo(), authority.getHost(), port));
    }
    return this;
  }

  @Override
  public UriBuilder setPath(Path path) {
    this.path = path;
    return this;
  }

  @Override
  public UriBuilder setQuery(@Nullable Query query) {
    this.query = query;
    return this;
  }

  @Override
  public UriBuilder setFragment(@Nullable Fragment fragment) {
    this.fragment = fragment;
    return this;
  }

  @Override
  public Uri build() {
    if (authority == null && (userInfo != null || port != null)) {
      throw new IllegalStateException("Cannot construct a uri with a userinfo or port but no host");
    }
    if (scheme == null) {
      if (authority == null && fragment == null) {
        return new PathAndQueryValue(path, query);
      } else {
        return new RelativeUrlValue(authority, path, query, fragment);
      }
    } else return buildUri(scheme, authority, path, query, fragment);
  }
}
