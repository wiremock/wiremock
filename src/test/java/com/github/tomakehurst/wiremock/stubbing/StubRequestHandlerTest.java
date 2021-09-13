/*
 * Copyright (C) 2011 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.testsupport.MockHttpResponder;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StubRequestHandlerTest {

  private StubServer stubServer;
  private ResponseRenderer responseRenderer;
  private MockHttpResponder httpResponder;
  private Admin admin;
  private RequestJournal requestJournal;

  private StubRequestHandler requestHandler;

  @BeforeEach
  public void init() {
    stubServer = mock(StubServer.class);
    responseRenderer = mock(ResponseRenderer.class);
    httpResponder = new MockHttpResponder();
    admin = mock(Admin.class);
    requestJournal = mock(RequestJournal.class);

    requestHandler =
        new StubRequestHandler(
            stubServer,
            responseRenderer,
            admin,
            Collections.<String, PostServeAction>emptyMap(),
            requestJournal,
            Collections.<RequestFilter>emptyList(),
            false);
  }

  @Test
  public void returnsResponseIndicatedByMappings() {
    when(stubServer.serveStubFor(any(Request.class)))
        .thenReturn(
            ServeEvent.of(
                mockRequest().asLoggedRequest(), new ResponseDefinition(200, "Body content")));

    Response mockResponse = response().status(200).body("Body content").build();
    when(responseRenderer.render(any(ServeEvent.class))).thenReturn(mockResponse);

    Request request = aRequest().withUrl("/the/required/resource").withMethod(GET).build();
    requestHandler.handle(request, httpResponder);
    Response response = httpResponder.response;

    assertThat(response.getStatus(), is(200));
    assertThat(response.getBodyAsString(), is("Body content"));
  }

  @Test
  public void shouldNotifyListenersOnRequest() {
    final Request request = aRequest().build();
    final RequestListener listener = mock(RequestListener.class);
    requestHandler.addRequestListener(listener);

    doReturn(ServeEvent.of(LoggedRequest.createFrom(request), ResponseDefinition.notConfigured()))
        .when(stubServer)
        .serveStubFor(request);
    when(responseRenderer.render(any(ServeEvent.class))).thenReturn(new Response.Builder().build());

    requestHandler.handle(request, httpResponder);
    verify(listener).requestReceived(eq(request), any(Response.class));
  }

  @Test
  public void shouldLogInfoOnRequest() {
    final Request request = aRequest().withUrl("/").withMethod(GET).withClientIp("1.2.3.5").build();

    doReturn(ServeEvent.forUnmatchedRequest(LoggedRequest.createFrom(request)))
        .when(stubServer)
        .serveStubFor(request);
    when(responseRenderer.render(any(ServeEvent.class))).thenReturn(new Response.Builder().build());

    TestNotifier notifier = TestNotifier.createAndSet();

    requestHandler.handle(request, httpResponder);
    notifier.revert();

    assertThat(notifier.getErrorMessages().isEmpty(), is(true));
    assertThat(notifier.getInfoMessages().size(), is(1));
    assertThat(notifier.getInfoMessages().get(0), containsString("1.2.3.5 - GET /"));
  }
}
