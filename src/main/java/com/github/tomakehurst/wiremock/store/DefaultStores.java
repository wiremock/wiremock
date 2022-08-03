/*
 * Copyright (C) 2022 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.store.files.FileSourceBlobStore;

public class DefaultStores implements Stores {

  private final FileSource fileRoot;

  private final StubMappingStore stubMappingStore;
  private final RequestJournalStore requestJournalStore;
  private final SettingsStore settingsStore;

  private final ScenariosStore scenariosStore;

  public DefaultStores(FileSource fileRoot) {
    this.fileRoot = fileRoot;

    this.stubMappingStore = new InMemoryStubMappingStore();
    this.requestJournalStore = new InMemoryRequestJournalStore();
    this.settingsStore = new InMemorySettingsStore();
    this.scenariosStore = new InMemoryScenariosStore();
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
  public BlobStore getBlobStore(String name) {
    return new FileSourceBlobStore(fileRoot.child(name));
  }
}
