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
import static org.wiremock.url.AbsoluteUrlBuilder.buildUrl;

import org.jspecify.annotations.Nullable;

final class AbsoluteUriBuilder extends AbstractUriMutator<AbsoluteUri.Builder>
    implements AbsoluteUri.Builder {

  AbsoluteUriBuilder(Scheme scheme) {
    this.scheme = scheme;
  }

  AbsoluteUriBuilder(AbsoluteUri uri) {
    super(uri);
  }

  @Override
  public AbsoluteUri.Builder setScheme(Scheme scheme) {
    return super.doSetScheme(requireNonNull(scheme));
  }

  @Override
  public AbsoluteUri.Builder setAuthority(@Nullable Authority authority) {
    return super.doSetAuthority(authority);
  }

  @Override
  public AbsoluteUri build() {
    if (authority == null && (userInfo != null || port != null)) {
      throw new IllegalStateException("Cannot construct a uri with a userinfo or port but no host");
    }
    return buildUri(requireNonNull(scheme), authority, path, query, fragment);
  }

  static AbsoluteUri buildUri(
      Scheme scheme,
      @Nullable Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    if (authority == null) {
      return OpaqueUri.of(scheme, path, query, fragment);
    } else {
      return buildUrl(scheme, authority, path, query, fragment);
    }
  }
}
