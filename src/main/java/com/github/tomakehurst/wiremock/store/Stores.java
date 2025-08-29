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

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;

import org.wiremock.annotations.Beta;

/** The interface Stores. */
@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public interface Stores extends StoresLifecycle {

  /** The enum Persistence type. */
  enum PersistenceType {
    /** Persistent persistence type. */
    PERSISTENT,
    /** Ephemeral persistence type. */
    EPHEMERAL
  }

  /**
   * Gets stub store.
   *
   * @return the stub store
   */
  StubMappingStore getStubStore();

  /**
   * Gets request journal store.
   *
   * @return the request journal store
   */
  RequestJournalStore getRequestJournalStore();

  /**
   * Gets settings store.
   *
   * @return the settings store
   */
  SettingsStore getSettingsStore();

  /**
   * Gets scenarios store.
   *
   * @return the scenarios store
   */
  ScenariosStore getScenariosStore();

  /**
   * Gets recorder state store.
   *
   * @return the recorder state store
   */
  RecorderStateStore getRecorderStateStore();

  /**
   * Gets mappings blob store.
   *
   * @return the mappings blob store
   */
  default BlobStore getMappingsBlobStore() {
    return getBlobStore(MAPPINGS_ROOT);
  }

  /**
   * Gets files blob store.
   *
   * @return the files blob store
   */
  default BlobStore getFilesBlobStore() {
    return getBlobStore(FILES_ROOT);
  }

  /**
   * Gets blob store.
   *
   * @param name the name
   * @return the blob store
   */
  BlobStore getBlobStore(String name);

  /**
   * Gets object store.
   *
   * @param name the name
   * @return the object store
   */
  default ObjectStore getObjectStore(String name) {
    return getObjectStore(name, PersistenceType.EPHEMERAL);
  }

  /**
   * Gets object store.
   *
   * @param name the name
   * @param persistenceTypeHint the persistence type hint
   * @return the object store
   */
  default ObjectStore getObjectStore(String name, PersistenceType persistenceTypeHint) {
    return getObjectStore(name, persistenceTypeHint, 10_000);
  }

  /**
   * Gets object store.
   *
   * @param name the name
   * @param persistenceTypeHint the persistence type hint
   * @param maxSize the max size
   * @return the object store
   */
  ObjectStore getObjectStore(String name, PersistenceType persistenceTypeHint, int maxSize);
}
