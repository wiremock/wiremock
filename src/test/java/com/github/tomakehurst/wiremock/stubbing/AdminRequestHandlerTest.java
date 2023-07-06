/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.Limit;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.BasicResponseRenderer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import com.github.tomakehurst.wiremock.testsupport.MockHttpResponder;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AdminRequestHandlerTest {
  private final Admin admin = mock(Admin.class);
  private MockHttpResponder httpResponder;

  private AdminRequestHandler handler;

  @BeforeEach
  public void init() {
    httpResponder = new MockHttpResponder();

    handler =
        new AdminRequestHandler(
            AdminRoutes.forClient(),
            admin,
            new BasicResponseRenderer(),
            new NoAuthenticator(),
            false,
            Collections.emptyList(),
            Collections.emptyList(),
            new DataTruncationSettings(Limit.UNLIMITED));
  }

  @Test
  void shouldSaveMappingsWhenSaveCalled() {
    Request request = aRequest().withUrl("/mappings/save").withMethod(POST).build();

    handler.handle(request, httpResponder, null);
    Response response = httpResponder.response;

    assertThat(response.getStatus(), is(HTTP_OK));
    verify(admin).saveMappings();
  }

  @Test
  void shouldClearMappingsJournalAndRequestDelayWhenResetCalled() {
    Request request = aRequest().withUrl("/reset").withMethod(POST).build();

    handler.handle(request, httpResponder, null);
    Response response = httpResponder.response;

    assertThat(response.getStatus(), is(HTTP_OK));
    verify(admin).resetAll();
  }

  @Test
  void shouldClearJournalWhenResetRequestsCalled() {
    Request request = aRequest().withUrl("/requests/reset").withMethod(POST).build();

    handler.handle(request, httpResponder, null);
    Response response = httpResponder.response;

    assertThat(response.getStatus(), is(HTTP_OK));
    verify(admin).resetRequests();
  }

  private static final String REQUEST_PATTERN_SAMPLE =
      "{												\n"
          + "	\"method\": \"DELETE\",						\n"
          + "	\"url\": \"/some/resource\"					\n"
          + "}												";

  @Test
  void shouldReturnCountOfMatchingRequests() {
    RequestPattern requestPattern = newRequestPattern(DELETE, urlEqualTo("/some/resource")).build();
    Mockito.when(admin.countRequestsMatching(requestPattern))
        .thenReturn(VerificationResult.withCount(5));

    handler.handle(
        aRequest()
            .withUrl("/requests/count")
            .withMethod(POST)
            .withBody(REQUEST_PATTERN_SAMPLE)
            .build(),
        httpResponder,
        null);
    Response response = httpResponder.response;

    assertThat(response.getStatus(), is(HTTP_OK));
    assertThat(
        response.getBodyAsString(),
        equalToJson("{ \"count\": 5, \"requestJournalDisabled\" : false}"));
  }

  private static final String GLOBAL_SETTINGS_JSON =
      "{												\n" + "	\"fixedDelay\": 2000						\n" + "}												";

  @Test
  void shouldUpdateGlobalSettings() {
    handler.handle(
        aRequest().withUrl("/settings").withMethod(POST).withBody(GLOBAL_SETTINGS_JSON).build(),
        httpResponder,
        null);

    GlobalSettings expectedSettings = GlobalSettings.builder().fixedDelay(2000).build();
    verify(admin).updateGlobalSettings(expectedSettings);
  }
}
