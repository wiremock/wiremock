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
package com.github.tomakehurst.wiremock.store;

import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.Test;

public class InMemoryRequestJournalStoreTest {

  @Test
  void addWithNullMaxEntriesRetainsAllEntries() {
    InMemoryRequestJournalStore store = new InMemoryRequestJournalStore(null);

    ServeEvent one = serveEvent("/one");
    ServeEvent two = serveEvent("/two");
    ServeEvent three = serveEvent("/three");

    store.add(one);
    store.add(two);
    store.add(three);

    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(
        store.getAllKeys().collect(toList()), contains(three.getId(), two.getId(), one.getId()));
  }

  @Test
  void addDoesNotEvictWhenEntryCountEqualsMaxEntries() {
    InMemoryRequestJournalStore store = new InMemoryRequestJournalStore(3);

    ServeEvent one = serveEvent("/one");
    ServeEvent two = serveEvent("/two");
    ServeEvent three = serveEvent("/three");

    store.add(one);
    store.add(two);
    store.add(three);

    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(
        store.getAllKeys().collect(toList()), contains(three.getId(), two.getId(), one.getId()));
  }

  @Test
  void addEvictsOldestEntryWhenExceedingMaxEntriesByOne() {
    InMemoryRequestJournalStore store = new InMemoryRequestJournalStore(2);

    ServeEvent one = serveEvent("/one");
    ServeEvent two = serveEvent("/two");
    ServeEvent three = serveEvent("/three");

    store.add(one);
    store.add(two);
    store.add(three);

    assertThat(store.getAllKeys().count(), is(2L));
    assertThat(store.get(one.getId()).isPresent(), is(false));
    assertThat(store.get(two.getId()).isPresent(), is(true));
    assertThat(store.get(three.getId()).isPresent(), is(true));
  }

  @Test
  void addWithMaxEntriesOfZeroLeavesStoreEmpty() {
    InMemoryRequestJournalStore store = new InMemoryRequestJournalStore(0);

    ServeEvent one = serveEvent("/one");
    store.add(one);

    assertThat(store.getAllKeys().count(), is(0L));
    assertThat(store.get(one.getId()).isPresent(), is(false));
  }

  private static ServeEvent serveEvent(String url) {
    return ServeEvent.of(createFrom(aRequest(url).withUrl(url).build()));
  }
}
