/*
 * Copyright (C) 2016-2021 Thomas Akehurst
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
package ignored;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NearMissExampleTest {

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(options().dynamicPort().withRootDirectory("src/main/resources/empty"))
          .failOnUnmatchedRequests(true)
          .build();

  WireMockTestClient client;

  @BeforeEach
  public void init() {
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  public void showFullUnmatchedVerification() throws Exception {
    client.get("/some-other-thing");
    client.get("/totally-something-else");
    client.get("/whatever");
    client.post(
        "/my-near-miss",
        new StringEntity("{\"data\": { \"one\": 1}}", APPLICATION_JSON),
        withHeader("Content-Type", "application/json"),
        withHeader("X-Expected", "yes"),
        withHeader("X-Matched-1", "yes"),
        withHeader("Cookie", "this=that"),
        withHeader(
            "Authorization",
            new BasicCredentials("user", "wrong-pass").asAuthorizationHeaderValue()));

    wm.verify(
        postRequestedFor(urlEqualTo("/a-near-miss"))
            .withHeader("Content-Type", equalTo("text/json"))
            .withHeader("X-Expected", equalTo("yes"))
            .withHeader("X-Matched-1", matching("ye.*"))
            .withHeader("X-Matched-2", containing("no"))
            .withCookie("this", equalTo("other"))
            .withBasicAuth(new BasicCredentials("user", "pass"))
            .withRequestBody(equalToJson("{\"data\": { \"two\": 1}}")));
  }

  @Test
  public void showSingleUnmatchedRequest() {
    wm.stubFor(get(urlEqualTo("/hit")).willReturn(aResponse().withStatus(200)));
    client.get("/near-misssss");
  }

  @Test
  public void showManyUnmatchedRequests() {
    wm.stubFor(get(urlEqualTo("/hit")).willReturn(aResponse().withStatus(200)));
    client.get("/near-misssss");
    client.get("/hat");
    client.get("/whatevs");
  }
}
