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

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

abstract non-sealed class AbstractUriValue<NORMALISED extends Uri> implements Uri {

  protected final @Nullable Scheme scheme;
  protected final @Nullable Authority authority;
  protected final Path path;
  protected final @Nullable Query query;
  protected final @Nullable Fragment fragment;

  AbstractUriValue(
      @Nullable Scheme scheme,
      @Nullable Authority authority,
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
  public @Nullable Scheme getScheme() {
    return scheme;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Uri other)) {
      return false;
    }

    Class<? extends Uri> oneClass = ((Uri) this).getClass();
    Class<? extends Uri> otherClass = other.getClass();
    return shareSameSuperTypes(
            oneClass,
            otherClass,
            Origin.class,
            ServersideAbsoluteUrl.class,
            AbsoluteUrl.class,
            OpaqueUri.class,
            SchemeRelativeUrl.class,
            RelativeUrl.class,
            PathAndQuery.class)
        && Objects.equals(getScheme(), other.getScheme())
        && Objects.equals(getAuthority(), other.getAuthority())
        && Objects.equals(getPath(), other.getPath())
        && Objects.equals(getQuery(), other.getQuery())
        && Objects.equals(getFragment(), other.getFragment());
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean shareSameSuperTypes(
      Class<?> oneClass, Class<?> otherClass, Class<?>... types) {
    for (Class<?> type : types) {
      if (oneClass.isAssignableFrom(type) != otherClass.isAssignableFrom(type)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getScheme(), getAuthority(), getPath(), getQuery(), getFragment());
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (getScheme() != null) {
      result.append(getScheme()).append(":");
    }
    if (getAuthority() != null) {
      result.append("//").append(getAuthority());
    }
    result.append(getPath());
    if (getQuery() != null) {
      result.append("?").append(getQuery());
    }
    if (getFragment() != null) {
      result.append("#").append(getFragment());
    }
    return result.toString();
  }
}
