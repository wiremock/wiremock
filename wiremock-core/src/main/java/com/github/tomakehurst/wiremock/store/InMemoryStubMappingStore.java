/*
 * Copyright (C) 2022-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

/**
 * Keeps an index of the stored stubs by path and by method, and matches requests against that index
 * rather than against {@link #getAll()}.
 *
 * <p>Subclasses that override {@link #add}, {@link #replace}, {@link #remove} or {@link #clear}
 * must call the overridden method so the index stays in step with the stubs. Overriding {@link
 * #getAll()} does not change which stubs are matched; override {@link #findCandidatesFor} for that.
 */
@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryStubMappingStore extends InMemoryMappingStore<StubMapping>
    implements StubMappingStore {

  private final StubMappingIndex index = new StubMappingIndex();

  @Override
  public Stream<StubMapping> findCandidatesFor(Request request) {
    return index.candidatesFor(request);
  }

  @Override
  public synchronized StubMapping add(StubMapping stub) {
    StubMapping added = super.add(stub);
    index.add(added);
    return added;
  }

  @Override
  public synchronized StubMapping replace(StubMapping existing, StubMapping updated) {
    Optional<StubMapping> stored = get(existing.getId());
    if (stored.isEmpty()) {
      return updated;
    }
    index.remove(stored.get());
    StubMapping replaced = super.replace(stored.get(), updated);
    index.add(replaced);
    return replaced;
  }

  @Override
  public synchronized void remove(UUID id) {
    get(id).ifPresent(index::remove);
    super.remove(id);
  }

  @Override
  public synchronized void clear() {
    index.clear();
    super.clear();
  }
}
