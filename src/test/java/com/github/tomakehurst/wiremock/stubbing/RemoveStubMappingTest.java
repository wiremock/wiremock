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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveStubMappingTest {

  StubLifecycleListener listener;

  @BeforeEach
  void init() {
    listener = mock(StubLifecycleListener.class);
    when(listener.beforeStubCreated(any())).thenCallRealMethod();
    when(listener.beforeStubEdited(any(), any())).thenCallRealMethod();
  }

  @Test
  void removingNonExistentStubByIdDoesNotTriggerStubListeners() {
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub = get("/").build();
      wireMockServer.addStubMapping(existingStub);
      wireMockServer.addStubMapping(post("/create").build());

      clearInvocations(listener);

      wireMockServer.removeStub(existingStub.getId());

      verify(listener).beforeStubRemoved(existingStub);
      verify(listener).afterStubRemoved(existingStub);
      verifyNoMoreInteractions(listener);
      clearInvocations(listener);

      wireMockServer.removeStub(UUID.randomUUID());

      verifyNoInteractions(listener);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void removingNonExistentStubDoesNotTriggerStubListeners() {
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub = get("/").build();
      wireMockServer.addStubMapping(existingStub);
      wireMockServer.addStubMapping(post("/create").build());

      clearInvocations(listener);

      wireMockServer.removeStub(existingStub);

      verify(listener).beforeStubRemoved(existingStub);
      verify(listener).afterStubRemoved(existingStub);
      verifyNoMoreInteractions(listener);
      clearInvocations(listener);

      wireMockServer.removeStub(put("/whatever").build());

      verifyNoInteractions(listener);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void removingNonExistentStubByIdDoesNotCallMappingSaver() {
    MappingsSource mappingsSource = mock();
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().mappingSource(mappingsSource).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub = get("/").persistent(true).build();
      wireMockServer.addStubMapping(existingStub);
      wireMockServer.addStubMapping(post("/create").persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStub(existingStub.getId());

      verify(mappingsSource).remove(existingStub.getId());
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      wireMockServer.removeStub(UUID.randomUUID());

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void removingNonExistentStubDoesNotCallMappingSaver() {
    MappingsSource mappingsSource = mock();
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().mappingSource(mappingsSource).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub = get("/").persistent(true).build();
      wireMockServer.addStubMapping(existingStub);
      wireMockServer.addStubMapping(post("/create").persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStub(existingStub);

      verify(mappingsSource).remove(existingStub.getId());
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      wireMockServer.removeStub(put("/whatever").build());

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void stubIsNotDeletedIfListenersPreventRemoval() {
    MappingsSource mappingsSource = mock();
    UUID disallowedStubId = UUID.randomUUID();
    doThrow(new RuntimeException("stop that"))
        .when(listener)
        .beforeStubRemoved(argThat(stub -> stub.getId().equals(disallowedStubId)));
    WireMockServer wireMockServer =
        new WireMockServer(
            wireMockConfig().mappingSource(mappingsSource).extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      UUID allowedStubId = UUID.randomUUID();
      wireMockServer.addStubMapping(get("/").withId(allowedStubId).persistent(true).build());
      wireMockServer.addStubMapping(
          post("/create").withId(disallowedStubId).persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStub(allowedStubId);

      verify(mappingsSource).remove(allowedStubId);
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      try {
        wireMockServer.removeStub(disallowedStubId);
      } catch (RuntimeException e) {
        assertThat(e.getMessage(), is("stop that"));
      }

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }
}
