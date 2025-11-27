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

public class IllegalRelativeRef extends IllegalUrlReference {

  public IllegalRelativeRef(String relativeRef) {
    this(relativeRef, null);
  }

  public IllegalRelativeRef(String illegalRelativeRef, @Nullable IllegalUrlPart cause) {
    this(illegalRelativeRef, "Illegal relative ref: `" + illegalRelativeRef + "`", cause);
  }

  public IllegalRelativeRef(
      String illegalRelativeRef, String message, @Nullable IllegalUrlPart cause) {
    super(illegalRelativeRef, message, cause);
  }
}
