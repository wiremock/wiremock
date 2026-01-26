/*
 * Copyright (C) 2026 Thomas Akehurst
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
import org.wiremock.url.RelativeUrl.Transformer;

final class RelativeUrlTransformer extends AbstractUriBaseBuilder<RelativeUrl.Transformer>
    implements RelativeUrl.Transformer {

  public RelativeUrlTransformer(RelativeUrl relativeUrl) {
    super(relativeUrl);
  }

  @Override
  public Transformer setScheme(@Nullable Scheme scheme) {
    return super.doSetScheme(scheme);
  }

  @Override
  public Transformer setAuthority(@Nullable Authority authority) {
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

final class RelativeUrlBuilder extends AbstractUriBaseBuilder<RelativeUrl.Builder>
    implements RelativeUrl.Builder {

  public RelativeUrlBuilder() {
    super();
  }

  public RelativeUrlBuilder(RelativeUrl url) {
    super(url);
  }

  @Override
  public RelativeUrl.Builder setAuthority(@Nullable Authority authority) {
    return super.doSetAuthority(authority);
  }

  @Override
  public RelativeUrl build() {
    return (RelativeUrl) super.build();
  }
}
