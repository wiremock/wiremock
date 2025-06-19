/*
 * Copyright (C) 2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options.DuplicatePolicy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class StubImportPersistenceTest {

  private final MappingsSource mappingsSource = mock();

  @RegisterExtension
  WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().mappingSource(mappingsSource).dynamicPort())
          .build();

  @Test
  void savesAllStubsTogetherWhenImportingMultipleStubs() {
    StubMapping existingStub = get("/existing/stub").persistent(true).willReturn(ok()).build();
    wm.addStubMapping(existingStub);
    wm.addStubMapping(
        put("/another/existing/stub").persistent(true).willReturn(noContent()).build());

    clearInvocations(mappingsSource);

    List<StubMapping> newStubs =
        List.of(
            get("/").persistent(true).willReturn(ok()).build(),
            get("/do/not/persist").willReturn(ok()).persistent(false).build(),
            post("/thing").willReturn(created()).persistent(true).build(),
            put("/thing/4")
                .withId(existingStub.getId())
                .willReturn(okJson("{}"))
                .persistent(true)
                .build());
    wm.importStubs(
        new StubImport(newStubs, new StubImport.Options(DuplicatePolicy.OVERWRITE, false)));
    verify(mappingsSource, times(1))
        .save(List.of(newStubs.get(3), newStubs.get(2), newStubs.get(0)));
    verifyNoMoreInteractions(mappingsSource);
  }

  @Test
  void doesNotSaveIgnoredStubsWhenImportingMultipleStubs() {
    StubMapping existingStub = get("/existing/stub").persistent(true).willReturn(ok()).build();
    wm.addStubMapping(existingStub);
    wm.addStubMapping(
        put("/another/existing/stub").persistent(true).willReturn(noContent()).build());

    clearInvocations(mappingsSource);

    List<StubMapping> newStubs =
        List.of(
            get("/").persistent(true).willReturn(ok()).build(),
            get("/do/not/persist").willReturn(ok()).persistent(false).build(),
            post("/thing").willReturn(created()).persistent(true).build(),
            put("/thing/4")
                .withId(existingStub.getId())
                .willReturn(okJson("{}"))
                .persistent(true)
                .build());
    wm.importStubs(new StubImport(newStubs, new StubImport.Options(DuplicatePolicy.IGNORE, false)));
    verify(mappingsSource, times(1)).save(List.of(newStubs.get(2), newStubs.get(0)));
    verifyNoMoreInteractions(mappingsSource);
  }

  @Test
  void setsAllStubsTogetherWhenImportingMultipleStubsAndRemovingNonImportedStubs() {
    wm.addStubMapping(get("/existing/stub").persistent(true).willReturn(ok()).build());
    wm.addStubMapping(
        put("/another/existing/stub").persistent(true).willReturn(noContent()).build());

    clearInvocations(mappingsSource);

    List<StubMapping> newStubs =
        List.of(
            get("/").persistent(true).willReturn(ok()).build(),
            get("/do/not/persist").willReturn(ok()).persistent(false).build(),
            post("/thing").willReturn(created()).persistent(true).build(),
            put("/thing/4").willReturn(okJson("{}")).persistent(true).build());
    wm.importStubs(
        new StubImport(newStubs, new StubImport.Options(DuplicatePolicy.OVERWRITE, true)));
    verify(mappingsSource, times(1))
        .setAll(List.of(newStubs.get(3), newStubs.get(2), newStubs.get(0)));
    verifyNoMoreInteractions(mappingsSource);
  }

  @Test
  void removesAllPersistedStubsWhenNoImportedStubsAreSetToPersistAndNonImportedStubsAreDeleted() {
    wm.addStubMapping(get("/existing/stub").persistent(true).willReturn(ok()).build());
    wm.addStubMapping(
        put("/another/existing/stub").persistent(true).willReturn(noContent()).build());

    clearInvocations(mappingsSource);

    List<StubMapping> newStubs =
        List.of(
            get("/").persistent(false).willReturn(ok()).build(),
            get("/do/not/persist").willReturn(ok()).persistent(false).build());
    wm.importStubs(
        new StubImport(newStubs, new StubImport.Options(DuplicatePolicy.OVERWRITE, true)));
    verify(mappingsSource, times(1)).setAll(List.of());
    verifyNoMoreInteractions(mappingsSource);
  }

  @Test
  void savesNothingWhenNoStubsAreSetToPersist() {
    StubMapping existingStub = get("/existing/stub").persistent(true).willReturn(ok()).build();
    wm.importStubs(new StubImport(List.of(existingStub), StubImport.Options.DEFAULTS));
    verify(mappingsSource, times(1)).save(List.of(existingStub));

    clearInvocations(mappingsSource);

    List<StubMapping> newStubs =
        List.of(
            get("/").persistent(false).willReturn(ok()).build(),
            get("/do/not/persist").willReturn(ok()).persistent(false).build());
    wm.importStubs(new StubImport(newStubs, StubImport.Options.DEFAULTS));
    verifyNoInteractions(mappingsSource);
  }
}
