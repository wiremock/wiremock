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

public interface Origin extends Url {

  @Override
  HostAndPort authority();

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default PathAndQuery pathAndQuery() {
    return PathAndQuery.EMPTY;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default Path path() {
    return Path.EMPTY;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  @Nullable
  default Query query() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  @Nullable
  default Fragment fragment() {
    return null;
  }

  @Override
  Url normalise();

  static Origin of(Scheme scheme, HostAndPort hostAndPort) {
    return new OriginValue(scheme, hostAndPort);
  }

  static Origin parse(String origin) {
    return OriginParser.INSTANCE.parse(origin);
  }
}
