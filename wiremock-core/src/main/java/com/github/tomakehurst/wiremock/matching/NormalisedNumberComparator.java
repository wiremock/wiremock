/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import java.math.BigDecimal;
import net.javacrumbs.jsonunit.core.NumberComparator;

public class NormalisedNumberComparator implements NumberComparator {
  @Override
  public boolean compare(BigDecimal expectedValue, BigDecimal actualValue, BigDecimal tolerance) {
    var normalisedExpectedValue = expectedValue.stripTrailingZeros();
    var normalisedActualValue = actualValue.stripTrailingZeros();
    if (tolerance != null) {
      var diff = normalisedExpectedValue.subtract(normalisedActualValue).abs();
      return diff.compareTo(tolerance) <= 0;
    } else {
      return normalisedExpectedValue.equals(normalisedActualValue);
    }
  }
}
