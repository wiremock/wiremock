/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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

final class PathAndQueryValue extends AbstractUriValue implements PathAndQuery {

  PathAndQueryValue(@Nullable String stringValue, Path path, @Nullable Query query) {
    super(stringValue, null, null, path, query, null);
    if (!path.isEmpty() && !path.isAbsolute()) {
      throw new IllegalPathAndQuery(
          this.toString(),
          "Illegal path and query: `"
              + this
              + "` - a path and query's path must be absolute or empty",
          new IllegalPath(
              path.toString(),
              "Illegal path: `" + path + "` - a path and query's path must be absolute or empty"));
    }
  }

  PathAndQueryValue(Path path, @Nullable Query query) {
    this(null, path, query);
  }
}
