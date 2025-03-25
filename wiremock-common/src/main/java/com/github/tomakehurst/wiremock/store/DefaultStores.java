/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.store.Stores.PersistenceType.EPHEMERAL;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.store.files.FileSourceBlobStore;
import com.github.tomakehurst.wiremock.store.files.FileSourceJsonObjectStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class DefaultStores implements Stores {

  private final FileSource fileRoot;

  private final StubMappingStore stubMappingStore;
  private final RequestJournalStore requestJournalStore;
  private final SettingsStore settingsStore;

  private final ScenariosStore scenariosStore;

  private final Map<String, ObjectStore> objectStores;

  public DefaultStores(FileSource fileRoot) {
    this.fileRoot = fileRoot;

    this.stubMappingStore = new InMemoryStubMappingStore();
    this.requestJournalStore = new InMemoryRequestJournalStore();
    this.settingsStore = new InMemorySettingsStore();
    this.scenariosStore = new InMemoryScenariosStore();

    objectStores = new ConcurrentHashMap<>();
  }

  @Override
  public StubMappingStore getStubStore() {
    return stubMappingStore;
  }

  @Override
  public RequestJournalStore getRequestJournalStore() {
    return requestJournalStore;
  }

  @Override
  public SettingsStore getSettingsStore() {
    return settingsStore;
  }

  @Override
  public ScenariosStore getScenariosStore() {
    return scenariosStore;
  }

  @Override
  public RecorderStateStore getRecorderStateStore() {
    return new InMemoryRecorderStateStore();
  }

  @Override
  public BlobStore getBlobStore(String name) {
    final FileSource child = fileRoot.child(name);
    return new FileSourceBlobStore(child);
  }

  @Override
  public ObjectStore getObjectStore(
      String name, PersistenceType persistenceTypeHint, int maxItems) {
    if (persistenceTypeHint == EPHEMERAL) {
      return objectStores.computeIfAbsent(name, n -> new InMemoryObjectStore(maxItems));
    } else {
      final FileSource child = fileRoot.child(name);
      return new FileSourceJsonObjectStore(child);
    }
  }

  @Override
  public void start() {}

  @Override
  public void stop() {}
}
