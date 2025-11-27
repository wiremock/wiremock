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

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.recording.Recorder;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options.DuplicatePolicy;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RecordedStubPersistenceTest {

  @Test
  void importsAllRecordedStubsInASingleRequest() {
    Admin admin = mock();
    Recorder recorder = new Recorder(admin, Extensions.NONE, mock(), mock());
    ArgumentCaptor<StubImport> importCaptor = ArgumentCaptor.captor();

    verifyNoInteractions(admin);

    List<ServeEvent> serveEvents =
        List.of(
            new ServeEvent(
                null,
                LoggedRequest.createFrom(mockRequest().method(GET).url("/")),
                null,
                null,
                LoggedResponse.from(Response.response().status(200).build(), UNLIMITED),
                false,
                Timing.UNTIMED,
                new LinkedBlockingDeque<>()),
            new ServeEvent(
                null,
                LoggedRequest.createFrom(mockRequest().method(POST).url("/persist-me")),
                null,
                null,
                LoggedResponse.from(Response.response().status(202).build(), UNLIMITED),
                false,
                Timing.UNTIMED,
                new LinkedBlockingDeque<>()));
    recorder.takeSnapshot(
        serveEvents, recordSpec().allowNonProxied(true).makeStubsPersistent(true).build());

    verify(admin, times(1)).importStubs(importCaptor.capture());
    verifyNoMoreInteractions(admin);

    StubImport stubImport = importCaptor.getValue();
    assertFalse(stubImport.getImportOptions().getDeleteAllNotInImport());
    assertThat(stubImport.getImportOptions().getDuplicatePolicy(), is(DuplicatePolicy.OVERWRITE));

    assertThat(stubImport.getMappings().get(0).getRequest().getUrl(), is("/"));
    assertThat(stubImport.getMappings().get(0).getResponse().getStatus(), is(200));
    assertTrue(stubImport.getMappings().get(0).shouldBePersisted());

    assertThat(stubImport.getMappings().get(1).getRequest().getUrl(), is("/persist-me"));
    assertThat(stubImport.getMappings().get(1).getResponse().getStatus(), is(202));
    assertTrue(stubImport.getMappings().get(1).shouldBePersisted());
  }
}
