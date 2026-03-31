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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MappingsSaveRollbackTest {

  private final MappingsSource mappingsSource = mock();

  @RegisterExtension
  WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().mappingSource(mappingsSource).dynamicPort())
          .build();

  @Test
  void rollsBackAddedStubWhenSaveFails() {
    doThrow(new RuntimeException("save failed")).when(mappingsSource).save(any(StubMapping.class));

    StubMapping stub = get("/new").persistent(true).willReturn(ok()).build();

    assertThrows(RuntimeException.class, () -> wm.addStubMapping(stub));

    assertFalse(wm.getStubMapping(stub.getId()).isPresent());
  }

  @Test
  void rollsBackEditedStubWhenSaveFails() {
    StubMapping original = get("/edit-me").persistent(true).willReturn(ok("original")).build();
    wm.addStubMapping(original);

    doThrow(new RuntimeException("save failed")).when(mappingsSource).save(any(StubMapping.class));

    StubMapping updated =
        get("/edit-me").withId(original.getId()).persistent(true).willReturn(ok("updated")).build();

    assertThrows(RuntimeException.class, () -> wm.editStubMapping(updated));

    StubMapping inMemory = wm.getStubMapping(original.getId()).getItem();
    assertEquals("original", inMemory.getResponse().getBody());
  }

  @Test
  void rollsBackRemovedStubWhenDeleteFails() {
    StubMapping stub = get("/remove-me").persistent(true).willReturn(ok()).build();
    wm.addStubMapping(stub);

    doThrow(new RuntimeException("delete failed")).when(mappingsSource).remove(any(UUID.class));

    assertThrows(RuntimeException.class, () -> wm.removeStubMapping(stub));

    assertTrue(wm.getStubMapping(stub.getId()).isPresent());
  }

  @Test
  void rollsBackBulkRemoveWhenDeleteFails() {
    StubMapping stub1 = get("/bulk-1").persistent(true).willReturn(ok()).build();
    StubMapping stub2 = post("/bulk-2").persistent(true).willReturn(ok()).build();
    wm.addStubMapping(stub1);
    wm.addStubMapping(stub2);

    doThrow(new RuntimeException("delete failed")).when(mappingsSource).remove(any(List.class));

    assertThrows(RuntimeException.class, () -> wm.removeStubMappings(List.of(stub1, stub2)));

    assertTrue(wm.getStubMapping(stub1.getId()).isPresent());
    assertTrue(wm.getStubMapping(stub2.getId()).isPresent());
  }

  @Test
  void rollsBackImportWhenSaveFails() {
    doThrow(new RuntimeException("save failed")).when(mappingsSource).save(any(List.class));

    StubMapping stub = get("/imported").persistent(true).willReturn(ok()).build();

    assertThrows(
        RuntimeException.class,
        () -> wm.importStubs(new StubImport(List.of(stub), StubImport.Options.DEFAULTS)));

    assertFalse(wm.getStubMapping(stub.getId()).isPresent());
  }

  @Test
  void rollsBackImportWithDeleteAllWhenSetAllFails() {
    StubMapping existing = get("/existing").persistent(true).willReturn(ok()).build();
    wm.addStubMapping(existing);

    doThrow(new RuntimeException("setAll failed")).when(mappingsSource).setAll(any(List.class));

    StubMapping imported = post("/imported").persistent(true).willReturn(ok()).build();

    assertThrows(
        RuntimeException.class,
        () ->
            wm.importStubs(
                new StubImport(
                    List.of(imported),
                    new StubImport.Options(StubImport.Options.DuplicatePolicy.OVERWRITE, true))));

    assertTrue(wm.getStubMapping(existing.getId()).isPresent());
    assertFalse(wm.getStubMapping(imported.getId()).isPresent());
  }
}
