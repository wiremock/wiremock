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
package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

@JsonInclude(Include.NON_NULL)
public sealed interface WhatWGUrlTestCase extends Comparable<WhatWGUrlTestCase>
    permits FailureWhatWGUrlTestCase, SuccessWhatWGUrlTestCase {

  boolean success();

  String input();

  @Nullable
  String context();

  @JsonProperty(value = "failure", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  default boolean failure() {
    return !success();
  }

  Comparator<WhatWGUrlTestCase> comparator =
      Comparator.comparing(WhatWGUrlTestCase::input) // input assumed non-null
          .thenComparing(
              WhatWGUrlTestCase::context, Comparator.nullsFirst(Comparator.naturalOrder()));

  @Override
  default int compareTo(WhatWGUrlTestCase o) {
    return comparator.compare(this, o);
  }
}
