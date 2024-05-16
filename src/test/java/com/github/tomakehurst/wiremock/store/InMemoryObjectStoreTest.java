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
  void sizeLimitRemainsConsistenWhenItemRemoved() {
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
}
