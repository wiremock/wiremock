/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.admin.tasks.SaveMappingsTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SaveMappingsTaskTest {

  private Admin mockAdmin = Mockito.mock(Admin.class);
  private Request mockRequest = mockRequest();

  private SaveMappingsTask saveMappingsTask = new SaveMappingsTask();

  @Test
  public void delegatesSavingMappingsToAdmin() {
    saveMappingsTask.execute(mockAdmin, ServeEvent.of(mockRequest), PathParams.empty());

    verify(mockAdmin).saveMappings();
  }

  @Test
  public void returnsOkResponse() {
    ResponseDefinition response =
        saveMappingsTask.execute(mockAdmin, ServeEvent.of(mockRequest), PathParams.empty());

    assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    verify(mockAdmin).saveMappings();
  }
}
