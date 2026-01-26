/*
 * Copyright (C) 2026 Thomas Akehurst
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Test;

public class QueryParameterTemplatedMatcherAcceptanceTest extends AcceptanceTestBase {

  @Test
  void matchesQueryParameterEqualToAnotherQueryParameter() {
    stubFor(
        get(urlPathEqualTo("/test"))
            .withQueryParam("param2", equalToTemplated("{{request.query.param1}}"))
            .willReturn(ok()));

    // Should NOT match: param1=foo, but param2=bar (not equal)
    assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));

    // Should match: param1=foo and param2=foo (equal)
    assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
  }

  @Test
  void doesNotMatchEqualToQueryParameterIfTemplatingNotEnabled() {
    stubFor(
        get(urlPathEqualTo("/test"))
            .withQueryParam("param2", equalTo("{{request.query.param1}}"))
            .willReturn(ok()));

    // Should not match even though they are equal as templating not enabled
    assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(404));
  }

  @Test
  void matchesEqualToQueryParameterUsingUpperHelper() {
    stubFor(
        get(urlPathEqualTo("/test"))
            .withQueryParam("param2", equalToTemplated("{{upper request.query.param1}}"))
            .willReturn(ok()));

    // param2 should match the uppercase version of param1
    assertThat(testClient.get("/test?param1=hello&param2=HELLO").statusCode(), is(200));
  }

  @Test
  void matchesEqualToQueryParameterFromJsonStubWithTemplating() {
    String json =
        """
          {
            "request": {
              "urlPath": "/test",
              "method": "GET",
              "queryParameters": {
                "param2": {
                  "equalTo": "{{request.query.param1}}",
                  "templated": true
                }
              }
            },
            "response": {
              "status": 200
            }
          }
          """;

    // Parse the JSON into a StubMapping object
    StubMapping stubMapping = Json.read(json, StubMapping.class);

    // Add the stub mapping to WireMock using importStubs
    WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

    assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(200));
    assertThat(testClient.get("/test?param1=hello&param2=wrong").statusCode(), is(404));
  }

  @Test
  void doesNotMatchEqualToQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
    String json =
        """
          {
            "request": {
              "urlPath": "/test",
              "method": "GET",
              "queryParameters": {
                "param2": {
                  "equalTo": "{{request.query.param1}}",
                  "templated": false
                }
              }
            },
            "response": {
              "status": 200
            }
          }
          """;

    // Parse the JSON into a StubMapping object
    StubMapping stubMapping = Json.read(json, StubMapping.class);

    // Add the stub mapping to WireMock using importStubs
    WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

    assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(404));
  }
}
