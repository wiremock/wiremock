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

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveStubMappingsTest {

  StubLifecycleListener listener;

  @BeforeEach
  void init() {
    listener = mock(StubLifecycleListener.class);
    when(listener.beforeStubCreated(any())).thenCallRealMethod();
    when(listener.beforeStubEdited(any(), any())).thenCallRealMethod();
  }

  @Test
  void removingNonExistentStubsByIdDoesNotTriggerStubListeners() {
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub1 = get("/").build();
      wireMockServer.addStubMapping(existingStub1);
      StubMapping existingStub2 = post("/create").build();
      wireMockServer.addStubMapping(existingStub2);
      wireMockServer.addStubMapping(put("/modify").build());

      clearInvocations(listener);

      wireMockServer.removeStubMappings(
          List.of(
              get("/whatever").withId(existingStub1.getId()).build(),
              get("/whatever").withId(existingStub2.getId()).build()));

      verify(listener).beforeStubRemoved(existingStub1);
      verify(listener).beforeStubRemoved(existingStub2);
      verify(listener).afterStubRemoved(existingStub1);
      verify(listener).afterStubRemoved(existingStub2);
      verifyNoMoreInteractions(listener);
      clearInvocations(listener);

      wireMockServer.removeStubMappings(
          List.of(get("/whatever").build(), get("/whatever").build()));

      verifyNoInteractions(listener);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void removingNonExistentStubByRequestMatchDoesNotTriggerStubListeners() {
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub1 = get("/").build();
      wireMockServer.addStubMapping(existingStub1);
      StubMapping existingStub2 = post("/create").build();
      wireMockServer.addStubMapping(existingStub2);
      wireMockServer.addStubMapping(put("/modify").build());

      clearInvocations(listener);

      wireMockServer.removeStubMappings(List.of(get("/").build(), post("/create").build()));

      verify(listener).beforeStubRemoved(existingStub1);
      verify(listener).afterStubRemoved(existingStub1);
      verify(listener).beforeStubRemoved(existingStub2);
      verify(listener).afterStubRemoved(existingStub2);
      verifyNoMoreInteractions(listener);
      clearInvocations(listener);

      wireMockServer.removeStubMappings(
          List.of(get("/whatever").build(), put("/whatever").build()));

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

      StubMapping existingStub1 = get("/").persistent(true).build();
      wireMockServer.addStubMapping(existingStub1);
      StubMapping existingStub2 = post("/create").persistent(true).build();
      wireMockServer.addStubMapping(existingStub2);
      wireMockServer.addStubMapping(put("/modify").persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStubMappings(
          List.of(
              get("/whatever").withId(existingStub1.getId()).build(),
              get("/whatever").withId(existingStub2.getId()).build()));

      verify(mappingsSource).remove(List.of(existingStub1.getId(), existingStub2.getId()));
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      wireMockServer.removeStubMappings(
          List.of(get("/whatever").build(), get("/whatever").build()));

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void removingNonExistentStubByRequestMatchDoesNotCallMappingSaver() {
    MappingsSource mappingsSource = mock();
    WireMockServer wireMockServer =
        new WireMockServer(wireMockConfig().mappingSource(mappingsSource).dynamicPort());
    try {
      wireMockServer.start();

      StubMapping existingStub1 = get("/").persistent(true).build();
      wireMockServer.addStubMapping(existingStub1);
      StubMapping existingStub2 = post("/create").persistent(true).build();
      wireMockServer.addStubMapping(existingStub2);
      wireMockServer.addStubMapping(put("/modify").persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStubMappings(List.of(get("/").build(), post("/create").build()));

      verify(mappingsSource).remove(List.of(existingStub1.getId(), existingStub2.getId()));
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      wireMockServer.removeStubMappings(
          List.of(get("/whatever").build(), put("/whatever").build()));

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }

  @Test
  void stubsAreNotDeletedIfListenersPreventRemoval() {
    MappingsSource mappingsSource = mock();
    List<UUID> disallowedStubIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    doThrow(new RuntimeException("stop that"))
        .when(listener)
        .beforeStubRemoved(argThat(stub -> disallowedStubIds.contains(stub.getId())));
    WireMockServer wireMockServer =
        new WireMockServer(
            wireMockConfig().mappingSource(mappingsSource).extensions(listener).dynamicPort());
    try {
      wireMockServer.start();

      UUID allowedStubId1 = UUID.randomUUID();
      wireMockServer.addStubMapping(get("/").withId(allowedStubId1).persistent(true).build());
      wireMockServer.addStubMapping(
          post("/create").withId(disallowedStubIds.get(0)).persistent(true).build());
      wireMockServer.addStubMapping(
          put("/modify").withId(disallowedStubIds.get(1)).persistent(true).build());
      UUID allowedStubId2 = UUID.randomUUID();
      wireMockServer.addStubMapping(
          delete("/remove").withId(allowedStubId2).persistent(true).build());

      clearInvocations(mappingsSource);

      wireMockServer.removeStub(allowedStubId1);

      verify(mappingsSource).remove(allowedStubId1);
      verifyNoMoreInteractions(mappingsSource);
      clearInvocations(mappingsSource);

      try {
        wireMockServer.removeStubMappings(
            List.of(
                get("/whatever").withId(disallowedStubIds.get(0)).build(),
                get("/whatever").withId(allowedStubId2).build(),
                get("/whatever").withId(disallowedStubIds.get(1)).build()));
      } catch (RuntimeException e) {
        assertThat(e.getMessage(), is("stop that"));
      }

      verifyNoInteractions(mappingsSource);

    } finally {
      wireMockServer.stop();
    }
  }
}
