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

final class AbsoluteUrlTransformer extends AbstractUriBaseBuilder<AbsoluteUrl.Transformer>
    implements AbsoluteUrl.Transformer {

  AbsoluteUrlTransformer(AbsoluteUrl url) {
    super(url);
  }

  @Override
  public AbsoluteUrl.Transformer setScheme(Scheme scheme) {
    return super.doSetScheme(requireNonNull(scheme));
  }

  @Override
  public AbsoluteUrl.Transformer setAuthority(Authority authority) {
    return super.doSetAuthority(requireNonNull(authority));
  }

  @Override
  public AbsoluteUrl build() {
    return (AbsoluteUrl) super.build();
  }
}

final class AbsoluteUrlBuilder extends AbstractUriBaseBuilder<AbsoluteUrl.Builder>
    implements AbsoluteUrl.Builder {

  AbsoluteUrlBuilder(Scheme scheme, Authority authority) {
    this.scheme = scheme;
    this.authority = authority;
  }

  AbsoluteUrlBuilder(AbsoluteUrl url) {
    super(url);
  }

  @Override
  public AbsoluteUrl.Builder setScheme(Scheme scheme) {
    return super.doSetScheme(requireNonNull(scheme));
  }

  @Override
  public AbsoluteUrl.Builder setAuthority(Authority authority) {
    return super.doSetAuthority(requireNonNull(authority));
  }

  @Override
  public AbsoluteUrl build() {
    return (AbsoluteUrl) super.build();
  }
}
