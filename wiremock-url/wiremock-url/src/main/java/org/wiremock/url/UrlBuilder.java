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

import org.jspecify.annotations.Nullable;

final class UrlTransformer extends AbstractUriBaseBuilder<UrlTransformer>
    implements Url.Transformer<UrlTransformer> {

  UrlTransformer(Url url) {
    super(url);
  }

  @Override
  public Url build() {
    Uri uri = super.build();
    if (uri instanceof Url url) {
      return url;
    } else {
      throw new IllegalUrl(uri.toString(), "Illegal url: `" + uri + "`; a url has an authority");
    }
  }

  @Override
  public UrlTransformer setScheme(@Nullable Scheme scheme) {
    return super.doSetScheme(scheme);
  }

  @Override
  public UrlTransformer setAuthority(@Nullable Authority authority) {
    return super.doSetAuthority(authority);
  }
}

final class UrlBuilder extends AbstractUriBaseBuilder<Url.Builder> implements Url.Builder {

  UrlBuilder() {
    super();
  }

  UrlBuilder(Uri uri) {
    super(uri);
  }

  @Override
  public Url.Builder setScheme(@Nullable Scheme scheme) {
    return super.doSetScheme(scheme);
  }

  @Override
  public Url.Builder setAuthority(@Nullable Authority authority) {
    return super.doSetAuthority(authority);
  }

  @Override
  public Url build() {
    Uri uri = super.build();
    if (uri instanceof Url url) {
      return url;
    } else {
      throw new IllegalUrl(uri.toString(), "Illegal url: `" + uri + "`; a url has an authority");
    }
  }
}
