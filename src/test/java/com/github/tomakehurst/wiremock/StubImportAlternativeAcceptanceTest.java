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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.common.ListFunctions.indexBy;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener.AlteredStubMapping;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener.StubMappingToAlter;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class StubImportAlternativeAcceptanceTest {

  private final MappingsSource mappingsSource = mock();
  private final StubLifecycleListener lifecycleListener =
      mock(StubLifecycleListener.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

  @RegisterExtension
  WireMockExtension wmExt =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .mappingSource(mappingsSource)
                  .extensions(lifecycleListener)
                  .dynamicPort())
          .configureStaticDsl(false)
          .build();

  static Stream<Arguments> duplicatePolicyAndDeleteCombinations() {
    return Stream.of(
        Arguments.of(
            StubImport.Options.DuplicatePolicy.OVERWRITE,
            true,
            List.of(
                "Created Persistent 7",
                "Created Transient 8",
                "Created Persistent 9",
                "Created Transient 10",
                "Edited Transient 5",
                "Edited Persistent 4",
                "Edited Transient 2",
                "Edited Persistent 1"),
            List.of(
                "Created Persistent 7",
                "Created Persistent 9",
                "Edited Persistent 4",
                "Edited Persistent 1"),
            List.of(
                "edit:Persistent 1->Edited Persistent 1",
                "edit:Persistent 2->Edited Transient 2",
                "edit:Transient 4->Edited Persistent 4",
                "edit:Transient 5->Edited Transient 5",
                "create:Created Persistent 7",
                "create:Created Transient 8",
                "create:Created Persistent 9",
                "create:Created Transient 10",
                "remove:Persistent 3",
                "remove:Transient 6")),
        Arguments.of(
            StubImport.Options.DuplicatePolicy.OVERWRITE,
            false,
            List.of(
                "Created Persistent 7",
                "Created Transient 8",
                "Created Persistent 9",
                "Created Transient 10",
                "Transient 6",
                "Edited Transient 5",
                "Edited Persistent 4",
                "Persistent 3",
                "Edited Transient 2",
                "Edited Persistent 1"),
            List.of(
                "Created Persistent 7",
                "Created Persistent 9",
                "Edited Persistent 4",
                "Edited Persistent 1"),
            List.of(
                "edit:Persistent 1->Edited Persistent 1",
                "edit:Persistent 2->Edited Transient 2",
                "edit:Transient 4->Edited Persistent 4",
                "edit:Transient 5->Edited Transient 5",
                "create:Created Persistent 7",
                "create:Created Transient 8",
                "create:Created Persistent 9",
                "create:Created Transient 10")),
        Arguments.of(
            StubImport.Options.DuplicatePolicy.IGNORE,
            true,
            List.of(
                "Created Persistent 7",
                "Created Transient 8",
                "Created Persistent 9",
                "Created Transient 10",
                "Transient 5",
                "Transient 4",
                "Persistent 2",
                "Persistent 1"),
            List.of("Created Persistent 7", "Created Persistent 9", "Persistent 2", "Persistent 1"),
            List.of(
                "create:Created Persistent 7",
                "create:Created Transient 8",
                "create:Created Persistent 9",
                "create:Created Transient 10",
                "remove:Persistent 3",
                "remove:Transient 6")),
        Arguments.of(
            StubImport.Options.DuplicatePolicy.IGNORE,
            false,
            List.of(
                "Created Persistent 7",
                "Created Transient 8",
                "Created Persistent 9",
                "Created Transient 10",
                "Transient 6",
                "Transient 5",
                "Transient 4",
                "Persistent 3",
                "Persistent 2",
                "Persistent 1"),
            List.of("Created Persistent 7", "Created Persistent 9"),
            List.of(
                "create:Created Persistent 7",
                "create:Created Transient 8",
                "create:Created Persistent 9",
                "create:Created Transient 10")));
  }

  @ParameterizedTest
  @MethodSource("duplicatePolicyAndDeleteCombinations")
  void importWithPersistentAndTransientStubs(
      StubImport.Options.DuplicatePolicy duplicatePolicy,
      boolean deleteAllNotInImport,
      List<String> expectedStubNames,
      List<String> expectedSavedNames,
      List<String> expectedAlterations) {

    // given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    UUID id4 = UUID.randomUUID();
    UUID id5 = UUID.randomUUID();
    UUID id6 = UUID.randomUUID();
    UUID id7 = UUID.randomUUID();
    UUID id8 = UUID.randomUUID();

    wmExt.addStubMapping(getStubMapping("/1", id1, true, "Persistent 1"));
    wmExt.addStubMapping(getStubMapping("/2", id2, true, "Persistent 2"));
    wmExt.addStubMapping(getStubMapping("/3", id3, true, "Persistent 3"));
    wmExt.addStubMapping(getStubMapping("/4", id4, false, "Transient 4"));
    wmExt.addStubMapping(getStubMapping("/5", id5, false, "Transient 5"));
    wmExt.addStubMapping(getStubMapping("/6", id6, false, "Transient 6"));

    // expect
    assertSameNames(
        wmExt.listAllStubMappings().getMappings(),
        List.of(
            "Transient 6",
            "Transient 5",
            "Transient 4",
            "Persistent 3",
            "Persistent 2",
            "Persistent 1"));

    clearInvocations(mappingsSource);
    clearInvocations(lifecycleListener);

    // when
    List<StubMapping> importStubs =
        new ArrayList<>(
            List.of(
                getStubMapping("/1-import", id1, true, "Edited Persistent 1"),
                getStubMapping("/2-import", id2, false, "Edited Transient 2"),
                getStubMapping("/4-import", id4, true, "Edited Persistent 4"),
                getStubMapping("/5-import", id5, false, "Edited Transient 5")));
    Collections.shuffle(importStubs);
    importStubs.addAll(
        List.of(
            getStubMapping("/7-import", id7, true, "Created Persistent 7"),
            getStubMapping("/8-import", id8, false, "Created Transient 8"),
            getStubMapping("/9-import", id7, true, "Created Persistent 9"),
            getStubMapping("/10-import", id8, false, "Created Transient 10")));

    wmExt.importStubs(
        new StubImport(importStubs, new StubImport.Options(duplicatePolicy, deleteAllNotInImport)));

    // then
    List<StubMapping> stubsAfterImport = wmExt.listAllStubMappings().getMappings();
    assertSameNames(stubsAfterImport, expectedStubNames);

    Map<String, StubMapping> stubsAfterImportByName =
        indexBy(stubsAfterImport, StubMapping::getName);
    List<StubMapping> expectedSavedStubs =
        expectedSavedNames.stream().map(stubsAfterImportByName::get).toList();

    // and
    if (deleteAllNotInImport) {
      verify(mappingsSource).setAll(expectedSavedStubs);
    } else {
      verify(mappingsSource).save(expectedSavedStubs);
    }
    verifyNoMoreInteractions(mappingsSource);

    // and the lifecycle listener was called with the same alterations before and after
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<StubMappingToAlter>> beforeCaptor = ArgumentCaptor.forClass(List.class);
    verify(lifecycleListener).beforeStubsAltered(beforeCaptor.capture());
    assertThat(
        describeAlterations(beforeCaptor.getValue()),
        containsInAnyOrder(expectedAlterations.toArray()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AlteredStubMapping>> afterCaptor = ArgumentCaptor.forClass(List.class);
    verify(lifecycleListener).afterStubsAltered(afterCaptor.capture());
    assertThat(
        describeAlterations(afterCaptor.getValue()),
        containsInAnyOrder(expectedAlterations.toArray()));
  }

  private static StubMapping getStubMapping(String url, UUID id1, boolean persistent, String name) {
    return get(url).withId(id1).persistent(persistent).withName(name).willReturn(ok()).build();
  }

  private static void assertSameNames(
      List<StubMapping> resultStubs, List<String> expectedStubNames) {
    List<String> resultNames = resultStubs.stream().map(StubMapping::getName).toList();

    assertThat(resultNames, is(expectedStubNames));
  }

  private static List<String> describeAlterations(List<?> alterations) {
    return alterations.stream()
        .map(
            alteration -> {
              if (alteration instanceof StubLifecycleListener.CreatedStubMapping created) {
                return "create:" + created.getStub().getName();
              } else if (alteration instanceof StubLifecycleListener.EditedStubMapping edited) {
                return "edit:"
                    + edited.getOldStub().getName()
                    + "->"
                    + edited.getNewStub().getName();
              } else if (alteration instanceof StubLifecycleListener.RemovedStubMapping removed) {
                return "remove:" + removed.getStub().getName();
              }
              throw new IllegalArgumentException("Unknown alteration type: " + alteration);
            })
        .toList();
  }
}
