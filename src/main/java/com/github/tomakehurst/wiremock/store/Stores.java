/*
 * Copyright (C) 2022-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;

import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public interface Stores extends StoresLifecycle {

  enum PersistenceType {
    PERSISTENT,
    EPHEMERAL
  }

  StubMappingStore getStubStore();

  RequestJournalStore getRequestJournalStore();

  SettingsStore getSettingsStore();

  ScenariosStore getScenariosStore();

  RecorderStateStore getRecorderStateStore();

  default BlobStore getMappingsBlobStore() {
    return getBlobStore(MAPPINGS_ROOT);
  }

  default BlobStore getFilesBlobStore() {
    return getBlobStore(FILES_ROOT);
  }

  BlobStore getBlobStore(String name);

  default ObjectStore getObjectStore(String name) {
    return getObjectStore(name, PersistenceType.EPHEMERAL);
  }

  default ObjectStore getObjectStore(String name, PersistenceType persistenceTypeHint) {
    return getObjectStore(name, persistenceTypeHint, 10_000);
  }

  ObjectStore getObjectStore(String name, PersistenceType persistenceTypeHint, int maxSize);
}
