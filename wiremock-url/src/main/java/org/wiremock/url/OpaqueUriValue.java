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

class OpaqueUriValue implements OpaqueUri {

  private final Scheme scheme;
  private final Path path;
  private final @Nullable Query query;
  private final @Nullable Fragment fragment;

  OpaqueUriValue(Scheme scheme, Path path, @Nullable Query query, @Nullable Fragment fragment) {
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
    return UriParser.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return UriParser.hashCode(this);
  }

  @Override
  public String toString() {
    return UriParser.toString(this);
  }

  @Override
  public OpaqueUri normalise() {

    Scheme normalisedScheme = scheme.normalise();
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }

    /*
    `whatever:/..//` is a URI without an Authority.
    Acording to the spec https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4
    `/..//` should normalise to `//`, so `whatever:/..//` should normalise to `whatever://`.
    However, this changes the semantics to now have an (empty) authority and an empty path.

    We have made an executive decision that if a URI without an Authority has a path that
    normalises to more than one `/` at the start, they will be treatd as a single `/`.
    */
    if (normalisedPath.toString().startsWith("//")) {
      normalisedPath = new PathValue(normalisedPath.toString().replaceFirst("^//+", "/"), true);
    }
    Query normalisedQuery = query == null ? null : query.normalise();
    Fragment normalisedFragment = fragment == null ? null : fragment.normalise();

    if (scheme.equals(normalisedScheme)
        && path.equals(normalisedPath)
        && Objects.equals(query, normalisedQuery)
        && Objects.equals(fragment, normalisedFragment)) {
      return this;
    } else {
      return new OpaqueUriValue(
          normalisedScheme, normalisedPath, normalisedQuery, normalisedFragment);
    }
  }
}
