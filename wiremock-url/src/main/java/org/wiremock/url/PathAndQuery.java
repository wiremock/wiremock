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

public interface PathAndQuery extends RelativeRef {

  PathAndQuery EMPTY = new PathAndQueryValue(Path.EMPTY, null);

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default Authority getAuthority() {
    return null;
  }

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default Fragment getFragment() {
    return null;
  }

  @Override
  PathAndQuery normalise();

  static PathAndQuery parse(CharSequence pathAndQuery) throws IllegalPathAndQuery {
    return PathAndQueryParser.INSTANCE.parse(pathAndQuery);
  }
}
