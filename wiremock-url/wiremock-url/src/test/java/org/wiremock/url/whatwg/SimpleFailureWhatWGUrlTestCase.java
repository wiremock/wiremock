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
package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties({"comment"})
public record SimpleFailureWhatWGUrlTestCase(
    // always present, never null, can be empty signifying empty input
    String input,
    // always present, can be null, never empty
    // 213 null base & failure
    //  60 present base & failure
    @Nullable String base)
    implements FailureWhatWGUrlTestCase {

  @Override
  @Nullable
  public String context() {
    return base;
  }
}
