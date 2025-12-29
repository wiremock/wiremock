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

import org.jspecify.annotations.Nullable;

public non-sealed interface Urn extends Uri {

  default boolean isUrl() {
    return false;
  }

  default boolean isUrn() {
    return true;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default @Nullable Authority getAuthority() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default @Nullable UserInfo getUserInfo() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default @Nullable Host getHost() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default @Nullable Port getPort() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default @Nullable Port getResolvedPort() {
    return null;
  }

  static Urn parse(CharSequence urn) throws IllegalUrn {
    return UrnParser.INSTANCE.parse(urn);
  }

  static Urn of(Scheme scheme, Path path) {
    return of(scheme, path, null, null);
  }

  static Urn of(Scheme scheme, Path path, Query query) {
    return of(scheme, path, query, null);
  }

  static Urn of(Scheme scheme, Path path, @Nullable Query query, @Nullable Fragment fragment) {
    return new UrnValue(scheme, path, query, fragment);
  }
}
