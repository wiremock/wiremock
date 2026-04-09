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
package com.github.tomakehurst.wiremock.core;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MappingsSaverTest {

  private final MappingsSaver saver = mock();

  {
    doCallRealMethod().when(saver).mutate(any(), any());
    doCallRealMethod().when(saver).remove(any(List.class));
  }

  @Test
  void defaultMutateDoesNotCallSaveWhenSaveListIsEmpty() {
    saver.mutate(List.of(), List.of(UUID.randomUUID()));

    verify(saver, never()).save(any(List.class));
  }

  @Test
  void defaultMutateDoesNotCallRemoveWhenRemoveListIsEmpty() {
    StubMapping stub = get("/test").willReturn(ok()).build();

    saver.mutate(List.of(stub), List.of());

    verify(saver, never()).remove(any(UUID.class));
  }

  @Test
  void defaultMutateCallsSaveAndRemoveWhenBothListsAreNonEmpty() {
    StubMapping stub = get("/test").willReturn(ok()).build();
    UUID id = UUID.randomUUID();

    saver.mutate(List.of(stub), List.of(id));

    verify(saver).save(List.of(stub));
    verify(saver).remove(id);
  }
}
