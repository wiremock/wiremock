/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ParameterUtilsTest {

  @Test
  void ensuresMapImmutabilityWhenAlreadyGoogleImmutable() {
    final ImmutableMap<String, String> inputMap = ImmutableMap.of("key", "value");

    Map<String, String> outputMap = ParameterUtils.ensureImmutable(inputMap);

    assertThrows(UnsupportedOperationException.class, () -> outputMap.put("not", "allowed"));
  }

  @Test
  void ensuresMapImmutabilityWhenAlreadyJvmImmutable() {
    final Map<String, String> inputMap = Map.of("key", "value");

    Map<String, String> outputMap = ParameterUtils.ensureImmutable(inputMap);

    assertThrows(UnsupportedOperationException.class, () -> outputMap.put("not", "allowed"));
  }

  @Test
  void ensuresMapImmutabilityWhenAlreadyMetadata() {
    final Map<String, Object> inputMap = Metadata.builder().attr("key", "value").build();

    Map<String, Object> outputMap = ParameterUtils.ensureImmutable(inputMap);

    assertThrows(UnsupportedOperationException.class, () -> outputMap.put("not", "allowed"));
  }

  @Test
  void ensuresMapImmutabilityWhenNotImmutable() {
    final Map<String, String> inputMap = new HashMap<>();
    inputMap.put("key", "value");

    Map<String, String> outputMap = ParameterUtils.ensureImmutable(inputMap);

    assertThrows(UnsupportedOperationException.class, () -> outputMap.put("not", "allowed"));
  }
}
