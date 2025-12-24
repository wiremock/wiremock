/*
 * Copyright (C) 2025 Thomas Akehurst
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

class UrnValue implements Urn {

  private final Scheme scheme;
  private final Path path;
  private final @Nullable Query query;
  private final @Nullable Fragment fragment;

  UrnValue(Scheme scheme, Path path, @Nullable Query query, @Nullable Fragment fragment) {
    this.scheme = scheme;
    this.path = path;
    this.query = query;
    this.fragment = fragment;
  }

  @Override
  public Scheme getScheme() {
    return scheme;
  }

  @Override
  public Uri resolve(UriReference other) {
    throw new UnsupportedOperationException();
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
  public Urn normalise() {

    Scheme normalisedScheme = scheme.normalise();
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }
    Query normalisedQuery = query == null ? null : query.normalise(normalisedScheme);
    Fragment normalisedFragment = fragment == null ? null : fragment.normalise();

    if (scheme.equals(normalisedScheme)
        && path.equals(normalisedPath)
        && Objects.equals(query, normalisedQuery)
        && Objects.equals(fragment, normalisedFragment)) {
      return this;
    } else {
      return new UrnValue(normalisedScheme, normalisedPath, normalisedQuery, normalisedFragment);
    }
  }
}
