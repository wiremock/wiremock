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

public class IllegalAuthority extends IllegalUriPart {

  public IllegalAuthority(String authority) {
    this(authority, message(authority));
  }

  public IllegalAuthority(String authority, String message) {
    this(authority, message, null);
  }

  public IllegalAuthority(String authority, IllegalUriPart cause) {
    this(authority, message(authority), cause);
  }

  public IllegalAuthority(String authority, String message, @Nullable IllegalUriPart cause) {
    super(authority, message, cause);
  }

  private static String message(String authority) {
    return "Illegal authority: `" + authority + "`";
  }
}
