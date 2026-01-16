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
 * Represents a path and query component combination, which is a type of relative reference.
 *
 * <p>A PathAndQuery consists of a path and an optional query, with no scheme, authority, or
 * fragment. It is typically used to represent the part of a URL after the authority.
 *
 * <p>Implementations must be immutable and thread-safe.
 */
public interface PathAndQuery extends RelativeUrl {

  /** An empty path and query with no components. */
  PathAndQuery EMPTY = new PathAndQueryValue(Path.EMPTY, null);

  /**
   * Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @Deprecated // no point ever calling on this subtype
  default Authority getAuthority() {
    return null;
  }

  /**
   * Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @Deprecated // no point ever calling on this subtype
  default Fragment getFragment() {
    return null;
  }

  /**
   * Returns a normalised form of this path and query.
   *
   * @return a normalised path and query
   */
  @Override
  PathAndQuery normalise();

  /**
   * Parses a string into a path and query.
   *
   * @param pathAndQuery the string to parse
   * @return the parsed path and query
   * @throws IllegalPathAndQuery if the string is not a valid path and query
   */
  static PathAndQuery parse(String pathAndQuery) throws IllegalPathAndQuery {
    return PathAndQueryParser.INSTANCE.parse(pathAndQuery);
  }
}
