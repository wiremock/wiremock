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

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

abstract non-sealed class AbstractAbsoluteUriValue<NORMALISED extends AbsoluteUri>
    extends AbstractUriValue implements AbsoluteUri {

  protected final Scheme nonNullScheme;

  AbstractAbsoluteUriValue(
      @Nullable String stringValue,
      Scheme scheme,
      @Nullable Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    super(stringValue, scheme, authority, path, query, fragment);
    this.nonNullScheme = requireNonNull(scheme);
  }

  @Override
  public Scheme getScheme() {
    return nonNullScheme;
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public @NonNull NORMALISED normalise() {
    Scheme normalisedScheme = scheme != null ? scheme.normalise() : null;
    Authority normalisedAuthority = getNormalisedAuthority(normalisedScheme);
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }
    Query normalisedQuery = query == null ? null : query.normalise();
    Fragment normalisedFragment = fragment == null ? null : fragment.normalise();
    var uri =
        (Objects.equals(normalisedScheme, scheme)
                && Objects.equals(normalisedAuthority, authority)
                && Objects.equals(normalisedPath, path)
                && Objects.equals(normalisedQuery, query)
                && Objects.equals(normalisedFragment, fragment))
            ? this
            : Uri.builder()
                .setScheme(normalisedScheme)
                .setAuthority(normalisedAuthority)
                .setPath(normalisedPath)
                .setQuery(normalisedQuery)
                .setFragment(normalisedFragment)
                .build();
    return getNormalised(uri);
  }

  @SuppressWarnings("unchecked")
  private NORMALISED getNormalised(Uri uri) {
    return (NORMALISED) uri;
  }

  private @Nullable Authority getNormalisedAuthority(@Nullable Scheme normalisedScheme) {
    if (authority == null) {
      return null;
    }
    return normalisedScheme == null ? authority.normalise() : authority.normalise(normalisedScheme);
  }
}
