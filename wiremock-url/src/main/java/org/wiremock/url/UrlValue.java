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
import org.jspecify.annotations.Nullable;

final class UrlValue implements Url {

  private final Scheme scheme;
  private final Authority authority;
  private final Path path;
  private final @Nullable Query query;
  private final @Nullable Fragment fragment;

  UrlValue(
      Scheme scheme,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {
    this.scheme = scheme;
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
  public Url normalise() {
    Scheme normalisedScheme = scheme.normalise();
    Authority normalisedAuthority = authority.normalise(normalisedScheme);
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }
    Query normalisedQuery = query == null ? null : query.normalise();
    Fragment normalisedFragment = fragment == null ? null : fragment.normalise();

    if (scheme.equals(normalisedScheme)
        && authority.equals(normalisedAuthority)
        && path.equals(normalisedPath)
        && Objects.equals(query, normalisedQuery)
        && Objects.equals(fragment, normalisedFragment)) {
      return this;
    } else {
      return (Url)
          UriReference.builder()
              .setScheme(normalisedScheme)
              .setAuthority(normalisedAuthority)
              .setPath(normalisedPath)
              .setQuery(normalisedQuery)
              .setFragment(normalisedFragment)
              .build();
    }
  }

  @Override
  public Scheme getScheme() {
    return scheme;
  }

  @Override
  public Authority getAuthority() {
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
