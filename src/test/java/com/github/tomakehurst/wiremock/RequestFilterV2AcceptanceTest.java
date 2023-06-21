/*
 * Copyright (C) 2019-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.requestfilter.*;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RequestFilterV2AcceptanceTest {

  private WireMockServer wm;
  private WireMockTestClient client;
  private String url;

  @Test
  public void filterCanContinueWithModifiedRequest() {
    initialise(new RequestHeaderModifyingFilter());

    wm.stubFor(get(url).withHeader("X-Modify-Me", equalTo("modified")).willReturn(ok()));

    WireMockResponse response = client.get(url, withHeader("X-Modify-Me", "original"));
    assertThat(response.statusCode(), is(200));
  }

  @Test
  public void filterCanStopWithResponse() {
    initialise(new StubAuthenticatingFilter());

    wm.stubFor(get(url).willReturn(ok()));

    WireMockResponse good = client.get(url, withHeader("Authorization", "Token 123"));
    assertThat(good.statusCode(), is(200));

    WireMockResponse bad = client.get(url);
    assertThat(bad.statusCode(), is(401));
  }

  @Test
  public void filtersAreChained() {
    initialise(
        new RequestHeaderAppendingFilter("A"),
        new RequestHeaderAppendingFilter("B"),
        new RequestHeaderAppendingFilter("C"));

    wm.stubFor(get(url).withHeader("X-Modify-Me", matching("_[ABC]{3}")).willReturn(ok()));

    WireMockResponse response = client.get(url, withHeader("X-Modify-Me", "_"));
    assertThat(response.statusCode(), is(200));
  }

  @Test
  public void filterCanBeAppliedToAdmin() {
    initialise(new AdminAuthenticatingFilter());

    wm.stubFor(get(url).willReturn(ok()));

    String adminUrl = "/__admin/mappings";
    WireMockResponse good = client.get(adminUrl, withHeader("Authorization", "Token 123"));
    assertThat(good.statusCode(), is(200));

    WireMockResponse bad = client.get(adminUrl);
    assertThat(bad.statusCode(), is(401));

    // Stubs are unaffected
    WireMockResponse stub = client.get(url);
    assertThat(stub.statusCode(), is(200));
  }

  @Test
  public void filterCanBeAppliedToStubs() {
    initialise(new StubAuthenticatingFilter());

    wm.stubFor(get(url).willReturn(ok()));

    String adminUrl = "/__admin/mappings";
    WireMockResponse good = client.get(url, withHeader("Authorization", "Token 123"));
    assertThat(good.statusCode(), is(200));

    WireMockResponse bad = client.get(url);
    assertThat(bad.statusCode(), is(401));

    // Admin routes are unaffected
    WireMockResponse stub = client.get(adminUrl);
    assertThat(stub.statusCode(), is(200));
  }

  @Test
  public void filterCanBeAppliedToStubsAndAdmin() {
    initialise(new BothAuthenticatingFilter());

    wm.stubFor(get(url).willReturn(ok()));

    String adminUrl = "/__admin/mappings";

    WireMockResponse stub = client.get(url);
    assertThat(stub.statusCode(), is(401));

    WireMockResponse admin = client.get(adminUrl);
    assertThat(admin.statusCode(), is(401));
  }

  @Test
  public void wrappedRequestsAreUsedWhenProxying() {
    WireMockServer proxyTarget = new WireMockServer(wireMockConfig().dynamicPort());
    proxyTarget.start();
    initialise(new PathModifyingStubFilter());

    wm.stubFor(
        get(anyUrl())
            .willReturn(aResponse().proxiedFrom("http://localhost:" + proxyTarget.port())));
    proxyTarget.stubFor(get("/prefix/subpath/item").willReturn(ok("From the proxy")));

    assertThat(client.get("/subpath/item").content(), is("From the proxy"));

    proxyTarget.stop();
  }

  @Test
  void stubRequestFilterCanAddSubEvents() {
    initialise(
        new StubRequestFilterV2() {
          @Override
          public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
            String path = URI.create(request.getUrl()).getPath();
            serveEvent.appendSubEvent("REQ_PATH", Map.of("path", path));
            return RequestFilterAction.continueWith(request);
          }

          @Override
          public String getName() {
            return "sub-event-adding-filter";
          }
        });

    wm.stubFor(any(anyUrl()).willReturn(ok()));

    client.get("/find-this-path");

    SubEvent subEvent = wm.getAllServeEvents().get(0).getSubEvents().stream().findFirst().get();
    assertThat(subEvent.getType(), is("REQ_PATH"));
    assertThat(subEvent.getDataAs(Map.class).get("path"), is("/find-this-path"));
  }

  @BeforeEach
  public void init() {
    url = "/" + RandomStringUtils.randomAlphabetic(5);
  }

  @AfterEach
  public void stopServer() {
    wm.stop();
  }

  private void initialise(RequestFilterV2... filters) {
    wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(filters));
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  public static class RequestHeaderModifyingFilter implements StubRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      Request newRequest =
          RequestWrapper.create()
              .transformHeader("X-Modify-Me", values -> Collections.singletonList("modified"))
              .wrap(request);

      return RequestFilterAction.continueWith(newRequest);
    }

    @Override
    public String getName() {
      return "request-header-modifier";
    }
  }

  public static class StubAuthenticatingFilter implements StubRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      HttpHeader authHeader = request.header("Authorization");
      if (!authHeader.isPresent() || !authHeader.firstValue().equals("Token 123")) {
        return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
      }

      return RequestFilterAction.continueWith(request);
    }

    @Override
    public String getName() {
      return "stub-authenticator";
    }
  }

  public static class RequestHeaderAppendingFilter implements StubRequestFilterV2 {

    private final String value;

    public RequestHeaderAppendingFilter(String value) {
      this.value = value;
    }

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      Request newRequest =
          RequestWrapper.create()
              .transformHeader(
                  "X-Modify-Me",
                  existingValue -> Collections.singletonList(existingValue.get(0) + value))
              .wrap(request);

      return RequestFilterAction.continueWith(newRequest);
    }

    @Override
    public String getName() {
      return "request-header-appender-" + value;
    }
  }

  public static class AdminAuthenticatingFilter implements AdminRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      HttpHeader authHeader = request.header("Authorization");
      if (!authHeader.isPresent() || !authHeader.firstValue().equals("Token 123")) {
        return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
      }

      return RequestFilterAction.continueWith(request);
    }

    @Override
    public String getName() {
      return "admin-authenticator";
    }
  }

  public static class BothAuthenticatingFilter implements RequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      HttpHeader authHeader = request.header("Authorization");
      if (!authHeader.isPresent() || !authHeader.firstValue().equals("Token 123")) {
        return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
      }

      return RequestFilterAction.continueWith(request);
    }

    @Override
    public boolean applyToAdmin() {
      return true;
    }

    @Override
    public boolean applyToStubs() {
      return true;
    }

    @Override
    public String getName() {
      return "both-authenticator";
    }
  }

  public static class PathModifyingStubFilter implements StubRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
      Request wrappedRequest =
          RequestWrapper.create()
              .transformAbsoluteUrl(url -> url.replace("/subpath", "/prefix/subpath"))
              .wrap(request);

      return RequestFilterAction.continueWith(wrappedRequest);
    }

    @Override
    public String getName() {
      return "path-mod-filter";
    }
  }
}
