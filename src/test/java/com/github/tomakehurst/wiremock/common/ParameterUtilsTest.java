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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ParameterUtilsTest {

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyGoogleImmutableList() {
    final ImmutableList<String> input = ImmutableList.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyGoogleImmutableSet() {
    final ImmutableSet<String> input = ImmutableSet.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyGoogleImmutableSortedSet() {
    final ImmutableSortedSet<String> input = ImmutableSortedSet.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyGoogleImmutableMultiset() {
    final ImmutableMultiset<String> input = ImmutableMultiset.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyJvmImmutableList() {
    final List<String> input = List.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenAlreadyJvmImmutableSet() {
    final Set<String> input = Set.of("a", "b");

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenMutableArrayList() {
    final List<String> input = new ArrayList<>(List.of("a", "b"));

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

  @Test
  void ensuresCollectionImmutabilityWhenMutableHashSet() {
    final Set<String> input = new HashSet<>(Set.of("a", "b"));

    Collection<String> output = ParameterUtils.ensureImmutable(input);

    assertThrows(UnsupportedOperationException.class, () -> output.add("not allowed"));
  }

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
