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

abstract class AbstractAbsoluteUrlValue<NORMALISED extends AbsoluteUrl>
    extends AbstractAbsoluteUriValue<NORMALISED> implements AbsoluteUrl {

  protected final Authority nonNullAuthority;

  AbstractAbsoluteUrlValue(
      Scheme scheme,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    super(scheme, authority, path, query, fragment);
    this.nonNullAuthority = requireNonNull(authority);

    if (!path.isEmpty() && !path.isAbsolute()) {
      throw new IllegalAbsoluteUrl(
          this.toString(),
          "Illegal absolute url: `"+ this +"` - an absolute url's path must be absolute or empty, was `"+path+"`",
          new IllegalPath(path.toString(), "Illegal path: `" + path + "` - must be absolute or empty"));
    }
  }

  @Override
  public Authority getAuthority() {
    return nonNullAuthority;
  }
}
