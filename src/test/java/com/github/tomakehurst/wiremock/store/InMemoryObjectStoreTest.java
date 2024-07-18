/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class InMemoryObjectStoreTest {

  @Test
  void respectsSpecifiedLimitWhenPutting() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    assertThat(store.getAllKeys().count(), is(3L));

    store.put("four", "4");
    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(store.getAllKeys().collect(toList()), hasItems("two", "three", "four"));
  }

  @Test
  void respectsSpecifiedLimitWhenComputing() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    assertThat(store.getAllKeys().count(), is(3L));

    store.compute("four", current -> "4");

    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(store.getAllKeys().collect(toList()), hasItems("two", "three", "four"));
  }

  @Test
  void removesLeastRecentlyAccessedWhenPuttingInExcessOfLimit() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    store.get("one");

    store.put("four", "4");
    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(store.getAllKeys().collect(toList()), hasItems("one", "three", "four"));
  }

  @Test
  void sizeLimitRemainsConsistentWhenItemRemoved() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");
    store.remove("two");

    assertThat(store.getAllKeys().count(), is(2L));

    store.put("four", "4");
    assertThat(store.getAllKeys().count(), is(3L));
    store.put("five", "5");
    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(store.getAllKeys().collect(toList()), hasItems("three", "four", "five"));
  }

  @Test
  void clearRemovesAllItems() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    store.clear();

    assertThat(store.getAllKeys().count(), is(0L));
  }

  @Test
  void tryingToRetrieveMissingKeyDoesNotEjectOtherKeys() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    assertThat(store.getAllKeys().count(), is(3L));

    store.get("four");

    assertThat(store.getAllKeys().count(), is(3L));
  }

  @Test
  void computingNullValueIsEquivalentToRemoval() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    store.compute("three", current -> null);

    assertThat(store.get("three"), is(Optional.empty()));
    assertThat(store.getAllKeys().collect(toList()), containsInAnyOrder("one", "two"));

    store.put("four", "4");

    assertThat(store.getAllKeys().collect(toList()), containsInAnyOrder("one", "two", "four"));

    AtomicReference<Object> previousValue = new AtomicReference<>("");
    store.compute("three", previousValue::getAndSet);
    assertThat(previousValue.get(), is(nullValue()));
  }

  @Test
  void eventEmittedOnPut() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    List<StoreEvent<String, Object>> events1 = new ArrayList<>();
    store.registerEventListener(events1::add);
    assertThat(events1, is(empty()));

    store.put("one", "1");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("one", null, "1")));

    List<StoreEvent<String, Object>> events2 = new ArrayList<>();
    store.registerEventListener(events2::add);
    assertThat(events2, is(empty()));
    events1.clear();

    store.put("two", "2");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("two", null, "2")));
    assertThat(events2, containsInAnyOrder(new StoreEvent<>("two", null, "2")));

    events1.clear();
    events2.clear();
    store.put("one", "3");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("one", "1", "3")));
    assertThat(events2, containsInAnyOrder(new StoreEvent<>("one", "1", "3")));
  }

  @Test
  void eventEmittedOnRemoval() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    List<StoreEvent<String, Object>> events1 = new ArrayList<>();
    store.registerEventListener(events1::add);
    assertThat(events1, is(empty()));

    store.remove("two");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("two", "2", null)));

    List<StoreEvent<String, Object>> events2 = new ArrayList<>();
    store.registerEventListener(events2::add);
    events1.clear();

    store.remove("three");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("three", "3", null)));
    assertThat(events2, containsInAnyOrder(new StoreEvent<>("three", "3", null)));

    events1.clear();
    events2.clear();
    // No event is emitted when nothing is removed.
    store.remove("does not exist");

    assertThat(events1, is(empty()));
    assertThat(events2, is(empty()));
  }

  @Test
  void eventEmittedOnCompute() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    List<StoreEvent<String, Object>> events = new ArrayList<>();
    store.registerEventListener(events::add);

    store.compute("one", o -> "1");

    assertThat(events, containsInAnyOrder(new StoreEvent<>("one", null, "1")));

    events.clear();
    store.compute("two", o -> "2");

    assertThat(events, containsInAnyOrder(new StoreEvent<>("two", null, "2")));

    events.clear();
    store.compute("one", o -> "3");

    assertThat(events, containsInAnyOrder(new StoreEvent<>("one", "1", "3")));
  }

  @Test
  void eventEmittedOnRemovalByMaxItemLimit() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    store.put("one", "1");
    store.put("two", "2");
    store.put("three", "3");

    List<StoreEvent<String, Object>> events = new ArrayList<>();
    store.registerEventListener(events::add);

    store.put("four", "4");

    assertThat(
        events,
        containsInAnyOrder(
            new StoreEvent<>("four", null, "4"), new StoreEvent<>("one", "1", null)));

    events.clear();
    store.compute("five", o -> "5");

    assertThat(
        events,
        containsInAnyOrder(
            new StoreEvent<>("five", null, "5"), new StoreEvent<>("two", "2", null)));
  }

  @Test
  void exceptionsThrownInAnEventHandlerAreCaught() {
    InMemoryObjectStore store = new InMemoryObjectStore(3);

    List<StoreEvent<String, Object>> events1 = new ArrayList<>();
    store.registerEventListener(events1::add);
    store.registerEventListener(
        event -> {
          throw new RuntimeException();
        });
    List<StoreEvent<String, Object>> events2 = new ArrayList<>();
    store.registerEventListener(events2::add);

    store.put("one", "1");

    assertThat(events1, containsInAnyOrder(new StoreEvent<>("one", null, "1")));
    assertThat(events2, containsInAnyOrder(new StoreEvent<>("one", null, "1")));
  }
}
