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

public sealed class IllegalUri extends IllegalUriReference permits IllegalUrl, IllegalOpaqueUri {

  public IllegalUri(String uri) {
    this(uri, null);
  }

  public IllegalUri(String uri, @Nullable IllegalUriPart cause) {
    this(uri, "Illegal URI: `" + uri + "`", cause);
  }

  public IllegalUri(String uri, String message, @Nullable IllegalUriPart cause) {
    super(uri, message, cause);
  }
}
