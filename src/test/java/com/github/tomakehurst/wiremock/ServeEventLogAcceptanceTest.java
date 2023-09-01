/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.admin.model.ServeEventQuery.ALL_UNMATCHED;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactly;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.isToday;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Dates;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

public class ServeEventLogAcceptanceTest extends AcceptanceTestBase {

  static Stubbing dsl = wireMockServer;

  @Test
  public void returnsRecordedRequestsMatchingOnMethodAndExactUrl() throws Exception {
    testClient.get("/return/this");
    testClient.get("/but/not/this");
    testClient.get("/return/this");
    testClient.get("/return/this");
    testClient.get("/but/not/this");

    List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/return/this")));

    assertThat(requests.size(), is(3));
    LoggedRequest firstRequest = requests.get(0);
    assertThat(firstRequest.getUrl(), is("/return/this"));
    assertThat(firstRequest.getMethod(), is(RequestMethod.GET));
    assertThat(firstRequest.getLoggedDate(), isToday());
    assertThat(parse(firstRequest.getLoggedDateString()), isToday());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void returnsRecordedRequestsMatchingOnMethodAndUrlRegex() {
    testClient.put("/should/return/this/request");
    testClient.get("/donot/return/this/request"); // Wrong method
    testClient.put("/also/return/this");
    testClient.put("/not/this");

    List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching(".*return/this.*")));

    assertThat(requests.size(), is(2));
    assertThat(
        requests, hasItems(withUrl("/should/return/this/request"), withUrl("/also/return/this")));
  }

  @Test
  public void returnsNoResultsAfterReset() {
    testClient.get("/blah");
    testClient.get("/blah");
    testClient.get("/blah");

    WireMock.reset();
    List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/blah")));

    assertThat(requests.size(), is(0));
  }

  @Test
  public void returnsNoResultsAfterRequestsReset() {
    testClient.get("/blah");
    testClient.get("/blah");
    testClient.get("/blah");

    WireMock.resetAllRequests();
    List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/blah")));

    assertThat(requests.size(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void resultsAreInOrderRequestsWereReceived() {
    testClient.get("/use/1");
    testClient.get("/ignore/1");
    testClient.get("/ignore/2");
    testClient.get("/use/2");
    testClient.get("/use/3");
    testClient.get("/ignore/3");
    testClient.get("/use/4");

    List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/use/.*")));

    assertThat(
        requests,
        hasExactly(withUrl("/use/1"), withUrl("/use/2"), withUrl("/use/3"), withUrl("/use/4")));
  }

  @Test
  public void requestBodyEncodingRemainsUtf8() {
    byte[] body = new byte[] {-38, -100}; // UTF-8 bytes for ڜ
    testClient.post(
        "/encoding", new ByteArrayEntity(body, ContentType.TEXT_PLAIN.withCharset(UTF_8)));

    List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/encoding")));
    LoggedRequest request = requests.get(0);
    assertThat(request.getBodyAsString(), is("ڜ"));
  }

  @Test
  public void getsAllServeEvents() {
    dsl.stubFor(
        get(urlPathEqualTo("/two")).willReturn(aResponse().withStatus(200).withBody("Exactly 2")));

    testClient.get("/one");
    testClient.get("/two");
    testClient.get("/three");

    List<ServeEvent> serveEvents = getAllServeEvents();

    ServeEvent three = serveEvents.get(0);
    assertThat(three.isNoExactMatch(), is(true));
    assertThat(three.getRequest().getUrl(), is("/three"));

    ServeEvent two = serveEvents.get(1);
    assertThat(two.isNoExactMatch(), is(false));
    assertThat(two.getRequest().getUrl(), is("/two"));
    assertThat(two.getResponse().getBody(), is("Exactly 2".getBytes()));
    assertThat(two.getResponse().getBodyAsString(), is("Exactly 2"));
    assertThat(
        two.getResponse().getBodyAsBase64(), is(Encoding.encodeBase64("Exactly 2".getBytes())));

    assertThat(serveEvents.get(2).isNoExactMatch(), is(true));
  }

  @Test
  public void getAllServeEventsPreservesBinaryBody() {
    dsl.stubFor(
        any(anyUrl())
            .willReturn(aResponse().withBody(MappingJsonSamples.BINARY_COMPRESSED_CONTENT)));

    testClient.get("/");

    List<ServeEvent> serveEvents = getAllServeEvents();
    ServeEvent serveEvent = serveEvents.get(0);
    assertThat(
        serveEvent.getResponse().getBody(), is(MappingJsonSamples.BINARY_COMPRESSED_CONTENT));
  }

  @Test
  public void getsAllServeEventsThatWereUnmatched() {
    dsl.stubFor(get("/match").willReturn(ok()));

    testClient.get("/match");
    testClient.get("/no-match");
    testClient.get("/just-wrong");
    testClient.get("/match");

    List<ServeEvent> serveEvents = getAllServeEvents(ALL_UNMATCHED);

    assertThat(serveEvents.size(), is(2));
    assertThat(serveEvents.get(0).getRequest().getUrl(), is("/just-wrong"));
    assertThat(serveEvents.get(1).getRequest().getUrl(), is("/no-match"));
  }

  @Test
  public void getsAllServeEventsThatMatchedStubId() {
    wm.stubFor(get("/one").willReturn(ok()));
    StubMapping stub2 = wm.stubFor(get("/two").willReturn(ok()));

    testClient.get("/two");
    testClient.get("/one");
    testClient.get("/one");
    testClient.get("/two");

    List<ServeEvent> serveEvents = getAllServeEvents(ServeEventQuery.forStubMapping(stub2));

    assertThat(serveEvents.size(), is(2));
    assertThat(serveEvents.get(0).getRequest().getUrl(), is("/two"));
    assertThat(serveEvents.get(1).getRequest().getUrl(), is("/two"));
  }

  private Matcher<LoggedRequest> withUrl(final String url) {
    return new TypeSafeMatcher<>() {
      @Override
      public boolean matchesSafely(LoggedRequest loggedRequest) {
        return loggedRequest.getUrl().equals(url);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("A logged request with url: " + url);
      }
    };
  }

  private static Date parse(String dateString) throws ParseException {
    return Dates.parse(dateString);
  }
}
