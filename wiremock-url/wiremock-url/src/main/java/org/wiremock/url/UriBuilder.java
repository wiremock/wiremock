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

import org.jspecify.annotations.Nullable;

final class UriBuilder extends AbstractUriBaseBuilder<Uri.Builder> implements Uri.Builder {

  UriBuilder() {
    super();
  }

  UriBuilder(Uri uri) {
    super(uri);
  }

  @Override
  public Uri.Builder setScheme(@Nullable Scheme scheme) {
    return super.doSetScheme(scheme);
  }

  @Override
  public Uri.Builder setAuthority(@Nullable Authority authority) {
    return super.doSetAuthority(authority);
  }
}
