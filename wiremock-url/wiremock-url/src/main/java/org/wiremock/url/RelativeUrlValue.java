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

final class RelativeUrlValue extends AbstractUriValue implements RelativeUrl {

  RelativeUrlValue(
      @Nullable String stringValue, Path path, @Nullable Query query, @Nullable Fragment fragment) {
    super(stringValue, null, null, path, query, fragment);
    if (!path.isEmpty()) {
      if (path.getFirstSegment().toString().contains(":")) {
        throw new IllegalRelativeUrl(
            this.toString(),
            "Illegal relative url: `"
                + this
                + "` - a relative url without authority's path may not contain a colon (`:`) in the first segment, as that implies a scheme",
            new IllegalPath(
                path.toString(),
                "Illegal path: `"
                    + path
                    + "` - may not contain a colon (`:`) in the first segment of a relative url with no authority"));
      } else if (path.toString().startsWith("//")) {
        throw new IllegalPathAndQuery(
            this.toString(),
            "Illegal relative url: `"
                + this
                + "` - a relative url without authority's path may not start with //, as that would make the first segment an authority",
            new IllegalPath(
                path.toString(),
                "Illegal path: `"
                    + path
                    + "` - may not start with // in a relative url with no authority"));
      }
    }
  }
}
