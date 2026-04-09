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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;

class SaveMappingsAtomicityTest {

  private final MappingsSource mappingsSource = mock();

  @RegisterExtension
  WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().mappingSource(mappingsSource).dynamicPort())
          .build();

  @Test
  void saveMappingsSavesAllStubsInSingleMutateCall() {
    wm.stubFor(get("/one").willReturn(ok("one")));
    wm.stubFor(get("/two").willReturn(ok("two")));
    wm.stubFor(get("/three").willReturn(ok("three")));

    wm.saveMappings();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<StubMapping>> saveCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<java.util.UUID>> removeCaptor = ArgumentCaptor.forClass(List.class);

    verify(mappingsSource).mutate(saveCaptor.capture(), removeCaptor.capture());
    verify(mappingsSource, never()).save(any(StubMapping.class));

    List<StubMapping> savedStubs = saveCaptor.getValue();
    assertEquals(3, savedStubs.size());
    assertTrue(savedStubs.stream().allMatch(StubMapping::shouldBePersisted));
    assertTrue(removeCaptor.getValue().isEmpty());
  }

  @Test
  void saveMappingsMarksAllStubsAsPersistent() {
    wm.stubFor(get("/not-persistent").willReturn(ok()));

    wm.saveMappings();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<StubMapping>> saveCaptor = ArgumentCaptor.forClass(List.class);

    verify(mappingsSource).mutate(saveCaptor.capture(), any());

    List<StubMapping> savedStubs = saveCaptor.getValue();
    assertEquals(1, savedStubs.size());
    assertTrue(savedStubs.get(0).shouldBePersisted());
  }
}
