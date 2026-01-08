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

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class RelativeRefValue implements RelativeRef {

  private final @Nullable Authority authority;
  private final Path path;
  private final @Nullable Query query;
  private final @Nullable Fragment fragment;

  RelativeRefValue(
      @Nullable Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    this.authority = authority;
    this.path = path;
    this.query = query;
    this.fragment = fragment;
  }

  @Override
  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public boolean equals(Object obj) {
    return UriReferenceParser.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return UriReferenceParser.hashCode(this);
  }

  @Override
  public String toString() {
    return UriReferenceParser.toString(this);
  }

  @Override
  public RelativeRef normalise() {
    Authority normalisedAuthority =
        Optional.ofNullable(authority).map(Authority::normalise).orElse(null);
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }
    Query normalisedQuery = query == null ? null : query.normalise();
    Fragment normalisedFragment = fragment == null ? null : fragment.normalise();
    if (Objects.equals(normalisedAuthority, authority)
        && Objects.equals(normalisedPath, path)
        && Objects.equals(normalisedQuery, query)
        && Objects.equals(normalisedFragment, fragment)) {
      return this;
    } else {
      return new RelativeRefValue(
          normalisedAuthority, normalisedPath, normalisedQuery, normalisedFragment);
    }
  }

  @Override
  public @Nullable Authority getAuthority() {
    return authority;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public @Nullable Query getQuery() {
    return query;
  }

  @Override
  public @Nullable Fragment getFragment() {
    return fragment;
  }
}
