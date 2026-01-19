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

final class PathAndQueryValue extends AbstractUriValue implements PathAndQuery {

  PathAndQueryValue(Path path, @Nullable Query query) {
    super(null, null, path, query, null);
    if (!path.isEmpty() && path.getSegments().get(0).toString().contains(":")) {
      throw new IllegalPathAndQuery(
          this.toString(),
          "Illegal path and query: `"+ this +"` - a relative url without authority's path may not contain a colon (`:`) in the first segment, as this is ambiguous",
          new IllegalPath(
              path.toString(),
              "Illegal path: `" + path + "` - may not contain a colon (`:`) in the first segment of a relative url with no authority"));
    }
  }
}
