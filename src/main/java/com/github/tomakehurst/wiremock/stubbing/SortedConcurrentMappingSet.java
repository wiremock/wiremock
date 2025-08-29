/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/** The type Sorted concurrent mapping set. */
public class SortedConcurrentMappingSet implements Iterable<StubMapping> {

  private final AtomicLong insertionCount;
  private final ConcurrentSkipListSet<StubMapping> mappingSet;

  /** Instantiates a new Sorted concurrent mapping set. */
  public SortedConcurrentMappingSet() {
    insertionCount = new AtomicLong();
    mappingSet = new ConcurrentSkipListSet<>(sortedByPriorityThenReverseInsertionOrder());
  }

  private Comparator<StubMapping> sortedByPriorityThenReverseInsertionOrder() {
    return (one, two) -> {
      int priorityComparison = one.comparePriorityWith(two);
      if (priorityComparison != 0) {
        return priorityComparison;
      }

      return Long.compare(two.getInsertionIndex(), one.getInsertionIndex());
    };
  }

  @Override
  public Iterator<StubMapping> iterator() {
    return mappingSet.iterator();
  }

  /**
   * Stream stream.
   *
   * @return the stream
   */
  public Stream<StubMapping> stream() {
    return mappingSet.stream();
  }

  /**
   * Add.
   *
   * @param mapping the mapping
   */
  public void add(StubMapping mapping) {
    mapping.setInsertionIndex(insertionCount.getAndIncrement());
    mappingSet.add(mapping);
  }

  /**
   * Remove boolean.
   *
   * @param mappingToRemove the mapping to remove
   * @return the boolean
   */
  public boolean remove(final StubMapping mappingToRemove) {
    boolean removedByUuid =
        mappingSet.removeIf(
            mapping ->
                mappingToRemove.getUuid() != null
                    && mapping.getUuid() != null
                    && mappingToRemove.getUuid().equals(mapping.getUuid()));

    boolean removedByRequestPattern =
        !removedByUuid
            && mappingSet.removeIf(
                mapping -> mappingToRemove.getRequest().equals(mapping.getRequest()));

    return removedByUuid || removedByRequestPattern;
  }

  /**
   * Replace boolean.
   *
   * @param existingStubMapping the existing stub mapping
   * @param newStubMapping the new stub mapping
   * @return the boolean
   */
  public boolean replace(StubMapping existingStubMapping, StubMapping newStubMapping) {

    if (mappingSet.remove(existingStubMapping)) {
      mappingSet.add(newStubMapping);
      return true;
    }
    return false;
  }

  /** Clear. */
  public void clear() {
    mappingSet.clear();
  }

  @Override
  public String toString() {
    return mappingSet.toString();
  }
}
