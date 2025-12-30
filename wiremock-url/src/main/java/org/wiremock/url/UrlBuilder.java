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

import org.jspecify.annotations.Nullable;
import org.wiremock.url.Url.Builder;

class UrlBuilder implements Builder {

  private Scheme scheme;
  private Authority authority;
  private Path path = Path.ROOT;
  @Nullable Query query = null;
  @Nullable Fragment fragment = null;

  UrlBuilder(Scheme scheme, Authority authority) {
    this.scheme = scheme;
    this.authority = authority;
  }

  UrlBuilder(Url url) {
    this.scheme = url.getScheme();
    this.authority = url.getAuthority();
    this.path = url.getPath();
    this.query = url.getQuery();
    this.fragment = url.getFragment();
  }

  @Override
  public Builder setScheme(Scheme scheme) {
    this.scheme = scheme;
    return this;
  }

  @Override
  public Builder setAuthority(Authority authority) {
    this.authority = authority;
    return this;
  }

  @Override
  public Builder setUserInfo(@Nullable UserInfo userInfo) {
    this.authority = Authority.of(userInfo, authority.getHost(), authority.getPort());
    return this;
  }

  @Override
  public Builder setHost(Host host) {
    this.authority = Authority.of(authority.getUserInfo(), host, authority.getPort());
    return this;
  }

  @Override
  public Builder setPort(@Nullable Port port) {
    this.authority = Authority.of(authority.getUserInfo(), authority.getHost(), port);
    return this;
  }

  @Override
  public Builder setPath(Path path) {
    this.path = path;
    return this;
  }

  @Override
  public Builder setQuery(@Nullable Query query) {
    this.query = query;
    return this;
  }

  @Override
  public Builder setFragment(@Nullable Fragment fragment) {
    this.fragment = fragment;
    return this;
  }

  @Override
  public Url build() {
    return buildUrl(scheme, authority, path, query, fragment);
  }

  static Url buildUrl(
      Scheme scheme,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    if (scheme.isNormalForm()
        && authority instanceof HostAndPort hostAndPort
        && hostAndPort.isNormalForm(scheme)
        && path.isEmpty()
        && query == null
        && fragment == null) {
      return new OriginValue(scheme, hostAndPort);
    } else {
      return new UrlValue(scheme, authority, path, query, fragment);
    }
  }
}
