/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HealthCheckTaskTest {

  private final Admin mockAdmin = Mockito.mock(Admin.class);
  private final Request mockRequest = mockRequest();
  private final HealthCheckTask healthCheckTask = new HealthCheckTask();

  @Test
  public void delegatesHealthChecksToAdmin() {
    healthCheckTask.execute(mockAdmin, ServeEvent.of(mockRequest), PathParams.empty());

    verify(mockAdmin).isHealthy();
  }

  @Test
  public void healthy() {
    when(mockAdmin.isHealthy()).thenReturn(true);
    ResponseDefinition response =
        healthCheckTask.execute(mockAdmin, ServeEvent.of(mockRequest), PathParams.empty());

    assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    assertThat(response.getStatusMessage(), is("Wiremock is ok"));
    assertThat(
        response.getStatusMessage(),
        equalTo(response.getReponseBody().asJson().get("message").asText()));
    assertThat(response.getReponseBody().asJson().get("status").asText(), is("healthy"));
  }

  @Test
  public void notHealthy() {
    ResponseDefinition response =
        healthCheckTask.execute(mockAdmin, ServeEvent.of(mockRequest), PathParams.empty());

    assertThat(response.getStatus(), is(HttpURLConnection.HTTP_UNAVAILABLE));
    assertThat(response.getStatusMessage(), is("Wiremock is not ok"));
    assertThat(
        response.getStatusMessage(),
        equalTo(response.getReponseBody().asJson().get("message").asText()));
    assertThat(response.getReponseBody().asJson().get("status").asText(), is("unhealthy"));
  }
}
