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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class RemoveStubsPersistenceTest {

  private final MappingsSource mappingsSource = mock();

  @RegisterExtension
  WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().mappingSource(mappingsSource).dynamicPort())
          .build();

  @Test
  void deletesAllStubsTogetherWhenRemovingMultipleStubs() {
    StubMapping stub1 =
        get("/stub-1")
            .withName("stub 1")
            .willReturn(aResponse().withStatus(200))
            .persistent(true)
            .build();
    StubMapping stub2 =
        get("/stub-2")
            .withName("stub 2")
            .willReturn(aResponse().withStatus(201))
            .persistent(false)
            .build();
    StubMapping stub3 =
        get("/stub-3")
            .withName("stub 3")
            .willReturn(aResponse().withStatus(202))
            .persistent(true)
            .build();
    StubMapping stub4 =
        get("/stub-4")
            .withName("stub 4")
            .willReturn(aResponse().withStatus(202))
            .persistent(true)
            .build();

    wm.addStubMapping(stub1);
    wm.addStubMapping(stub2);
    wm.addStubMapping(stub3);
    wm.addStubMapping(stub4);

    clearInvocations(mappingsSource);

    List<StubMapping> stubsToRemove =
        List.of(
            get("/whatever").withId(stub1.getId()).build(),
            get("/stub-4").withId(UUID.randomUUID()).build(),
            get("/whatever").withId(stub2.getId()).build());
    wm.removeStubMappings(stubsToRemove);
    verify(mappingsSource, times(1)).remove(List.of(stub1.getId(), stub4.getId()));
    verifyNoMoreInteractions(mappingsSource);
  }

  @Test
  void deletesNothingWhenNoRemovedStubsAreSetToPersist() {
    StubMapping stub1 =
        get("/stub-1")
            .withName("stub 1")
            .willReturn(aResponse().withStatus(200))
            .persistent(true)
            .build();
    StubMapping stub2 =
        get("/stub-2")
            .withName("stub 2")
            .willReturn(aResponse().withStatus(201))
            .persistent(false)
            .build();
    StubMapping stub3 =
        get("/stub-3")
            .withName("stub 3")
            .willReturn(aResponse().withStatus(202))
            .persistent(false)
            .build();
    StubMapping stub4 =
        get("/stub-4")
            .withName("stub 4")
            .willReturn(aResponse().withStatus(202))
            .persistent(false)
            .build();
    wm.importStubs(
        new StubImport(List.of(stub1, stub2, stub3, stub4), StubImport.Options.DEFAULTS));
    assertThat(
        wm.listAllStubMappings().getMappings(), containsInAnyOrder(stub1, stub2, stub3, stub4));
    clearInvocations(mappingsSource);

    wm.removeStubMappings(List.of(stub1));

    assertThat(wm.listAllStubMappings().getMappings(), containsInAnyOrder(stub2, stub3, stub4));
    verify(mappingsSource, times(1)).remove(List.of(stub1.getId()));

    clearInvocations(mappingsSource);

    wm.removeStubMappings(List.of(stub2, stub3, stub4));

    assertThat(wm.listAllStubMappings().getMappings(), empty());
    verifyNoInteractions(mappingsSource);
  }

  @Test
  void deletesNothingWhenNoStubsAreRemoved() {
    wm.addStubMapping(get("/existing/stub").persistent(true).willReturn(ok()).build());

    clearInvocations(mappingsSource);

    wm.removeStubMappings(List.of(get("/whatever").build()));
    verifyNoInteractions(mappingsSource);
  }

  @Test
  void deletesNothingWhenNoStubsAreProvided() {
    wm.addStubMapping(get("/existing/stub").persistent(true).willReturn(ok()).build());

    clearInvocations(mappingsSource);

    wm.removeStubMappings(List.of());
    verifyNoInteractions(mappingsSource);
  }
}
