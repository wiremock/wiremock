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

import org.jspecify.annotations.Nullable;
import org.wiremock.url.AbsoluteUrl.Builder;

final class AbsoluteUrlBuilder extends AbstractUriMutator<Builder> implements Builder {

  AbsoluteUrlBuilder(Scheme scheme, Authority authority) {
    this.scheme = scheme;
    this.authority = authority;
  }

  AbsoluteUrlBuilder(AbsoluteUrl url) {
    super(url);
  }

  @Override
  public Builder setScheme(Scheme scheme) {
    return super.doSetScheme(requireNonNull(scheme));
  }

  @Override
  public Builder setAuthority(Authority authority) {
    return super.doSetAuthority(requireNonNull(authority));
  }

  @Override
  public AbsoluteUrl build() {
    return buildUrl(requireNonNull(scheme), requireNonNull(authority), path, query, fragment);
  }

  static AbsoluteUrl buildUrl(
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
    } else if (fragment == null) {
      return new ServersideAbsoluteUrlValue(scheme, authority, path, query);
    } else {
      return new AbsoluteUrlValue(scheme, authority, path, query, fragment);
    }
  }
}
