/*
 * Copyright (C) 2013-2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import static com.google.common.base.Optional.absent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.admin.tasks.RemoveStubMappingTask;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.net.HttpURLConnection;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RemoveStubMappingTaskTest {

  private static final StubMapping MOCK_MAPPING = new StubMapping(null, new ResponseDefinition());
  private static final String TEST_UUID_STRING = "7ef8ddb5-b00f-4991-949f-824cd1e2d072";
  private static final UUID TEST_UUID = UUID.fromString(TEST_UUID_STRING);

  private final Admin mockAdmin = Mockito.mock(Admin.class);
  private final PathParams mockPathParams = Mockito.mock(PathParams.class);

  private final RemoveStubMappingTask removeStubMappingTask = new RemoveStubMappingTask();

  @Test
  public void delegatesDeletingMappingToAdmin() {
    when(mockPathParams.get("id")).thenReturn(TEST_UUID_STRING);
    when(mockAdmin.getStubMapping(TEST_UUID)).thenReturn(new SingleStubMappingResult(MOCK_MAPPING));

    removeStubMappingTask.execute(mockAdmin, null, mockPathParams);

    verify(mockAdmin).removeStubMapping(MOCK_MAPPING);
  }

  @Test
  public void returnsOKResponse() {
    when(mockPathParams.get("id")).thenReturn(TEST_UUID_STRING);
    when(mockAdmin.getStubMapping(TEST_UUID)).thenReturn(new SingleStubMappingResult(MOCK_MAPPING));

    ResponseDefinition response = removeStubMappingTask.execute(mockAdmin, null, mockPathParams);

    assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    verify(mockAdmin).removeStubMapping(MOCK_MAPPING);
  }

  @Test
  public void returnsNOT_FOUNDResponse() {
    when(mockPathParams.get("id")).thenReturn(TEST_UUID_STRING);
    when(mockAdmin.getStubMapping(TEST_UUID))
        .thenReturn(SingleStubMappingResult.fromOptional(absent()));

    ResponseDefinition response = removeStubMappingTask.execute(mockAdmin, null, mockPathParams);

    assertThat(response.getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
    verify(mockAdmin, never()).removeStubMapping(TEST_UUID);
  }
}
