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

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.Nullable;

final class SchemeRelativeUrlValue extends AbstractUriValue implements SchemeRelativeUrl {

  private final Authority nonNullAuthority;

  SchemeRelativeUrlValue(
      @Nullable String stringValue,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    super(stringValue, null, authority, path, query, fragment);
    this.nonNullAuthority = requireNonNull(authority);

    if (!path.isEmpty() && !path.isAbsolute()) {
      throw new IllegalSchemeRelativeUrl(
          this.toString(),
          "Illegal scheme relative url: `"
              + this
              + "` - a scheme relative url's path must be absolute or empty, was `"
              + path
              + "`",
          new IllegalPath(
              path.toString(), "Illegal path: `" + path + "` - must be absolute or empty"));
    }
  }

  @Override
  public Authority getAuthority() {
    return nonNullAuthority;
  }
}
