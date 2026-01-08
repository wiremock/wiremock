/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Prioritisable.DEFAULT_PRIORITY;

import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class SortedConcurrentPrioritisableSet<T extends Prioritisable> implements Iterable<T> {

  private final AtomicLong insertionCount;
  private final ConcurrentSkipListSet<T> set;

  public SortedConcurrentPrioritisableSet() {
    insertionCount = new AtomicLong();
    set = new ConcurrentSkipListSet<>(sortedByPriorityThenReverseInsertionOrder());
  }

  private Comparator<Prioritisable> sortedByPriorityThenReverseInsertionOrder() {
    return (one, two) -> {
      int priorityComparison = comparePriorityWith(one, two);
      if (priorityComparison != 0) {
        return priorityComparison;
      }

      return Long.compare(two.getInsertionIndex(), one.getInsertionIndex());
    };
  }

  private static int comparePriorityWith(Prioritisable one, Prioritisable two) {
    int thisPriority = getFirstNonNull(one.getPriority(), DEFAULT_PRIORITY);
    int otherPriority = getFirstNonNull(two.getPriority(), DEFAULT_PRIORITY);
    return thisPriority - otherPriority;
  }

  @Override
  public Iterator<T> iterator() {
    return set.iterator();
  }

  public Stream<T> stream() {
    return set.stream();
  }

  public T add(T mapping) {
    mapping = mapping.withInsertionIndex(insertionCount.getAndIncrement());
    boolean actuallyAdded = set.add(mapping);
    return mapping;
  }

  public boolean remove(final UUID mappingId) {
    return set.removeIf(mapping -> mappingId != null && mappingId.equals(mapping.getId()));
  }

  public T replace(T existingItem, T newItem) {
    if (set.remove(existingItem)) {
      set.add(newItem);
    }

    return newItem;
  }

  public void clear() {
    set.clear();
  }

  @Override
  public String toString() {
    return set.toString();
  }
}
