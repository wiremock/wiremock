/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RemoveUnmatchedStubMappingsTaskTest {

  private final Admin mockAdmin = Mockito.mock(Admin.class);

  private final AdminTask task = new RemoveUnmatchedStubMappingsTask();

  @Test
  void removesMappingsInASingleRequest() {
    List<StubMapping> stubMappings =
        List.of(get("/").willReturn(ok()).build(), post("/create").willReturn(created()).build());
    when(mockAdmin.findUnmatchedStubs())
        .thenReturn(
            new ListStubMappingsResult(
                stubMappings, new PaginatedResult.Meta(stubMappings.size())));

    task.execute(mockAdmin, ServeEvent.of(mockRequest()), PathParams.empty());

    verify(mockAdmin).findUnmatchedStubs();
    verify(mockAdmin).removeStubMappings(stubMappings);
    verifyNoMoreInteractions(mockAdmin);
  }
}
