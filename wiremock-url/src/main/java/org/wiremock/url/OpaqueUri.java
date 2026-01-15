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

import org.jspecify.annotations.Nullable;

/**
 * Represents a URI as defined in <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC
 * 3986</a> which does not have an {@link Authority}, such as {@code mailto:joan@example.com},
 * {@code file:/home/joan} or {@code aws:some:identifier}
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>
 */
public non-sealed interface OpaqueUri extends AbsoluteUri {

  default boolean isAbsoluteUrl() {
    return false;
  }

  default boolean isOpaqueUri() {
    return true;
  }

  @Override
  @Deprecated // no point ever calling on this subtype
  default @Nullable Authority getAuthority() {
    return null;
  }

  @Override
  @Deprecated // no point ever calling on this subtype
  default @Nullable UserInfo getUserInfo() {
    return null;
  }

  @Override
  @Deprecated // no point ever calling on this subtype
  default @Nullable Host getHost() {
    return null;
  }

  @Override
  @Deprecated // no point ever calling on this subtype
  default @Nullable Port getPort() {
    return null;
  }

  @Override
  @Deprecated // no point ever calling on this subtype
  default @Nullable Port getResolvedPort() {
    return null;
  }

  static OpaqueUri parse(String opaqueUri) throws IllegalOpaqueUri {
    return OpaqueUriParser.INSTANCE.parse(opaqueUri);
  }

  static OpaqueUri of(Scheme scheme, Path path) {
    return of(scheme, path, null, null);
  }

  static OpaqueUri of(Scheme scheme, Path path, Query query) {
    return of(scheme, path, query, null);
  }

  static OpaqueUri of(
      Scheme scheme, Path path, @Nullable Query query, @Nullable Fragment fragment) {
    return new OpaqueUriValue(scheme, path, query, fragment);
  }
}
