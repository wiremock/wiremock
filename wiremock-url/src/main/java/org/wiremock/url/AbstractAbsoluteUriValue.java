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

abstract non-sealed class AbstractAbsoluteUriValue<NORMALISED extends AbsoluteUri>
    extends AbstractUriValue<NORMALISED> implements AbsoluteUri {

  protected final Scheme scheme;

  AbstractAbsoluteUriValue(
      Scheme scheme,
      @Nullable Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    super(scheme, authority, path, query, fragment);
    this.scheme = requireNonNull(scheme);
  }

  @Override
  public Scheme getScheme() {
    return scheme;
  }
}
