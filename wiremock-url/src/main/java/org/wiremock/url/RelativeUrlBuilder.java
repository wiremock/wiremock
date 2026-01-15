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

import org.wiremock.url.RelativeUrl.Builder;

final class RelativeUrlBuilder extends AbstractUriMutator<Builder> implements Builder {

  RelativeUrlBuilder() {}

  RelativeUrlBuilder(RelativeUrl url) {
    super(url);
  }

  @Override
  public RelativeUrl build() {
    if (authority == null && (userInfo != null || port != null)) {
      throw new IllegalStateException("Cannot construct a uri with a userinfo or port but no host");
    }
    if (authority == null && fragment == null) {
      return new PathAndQueryValue(path, query);
    } else {
      return new RelativeUrlValue(authority, path, query, fragment);
    }
  }
}
