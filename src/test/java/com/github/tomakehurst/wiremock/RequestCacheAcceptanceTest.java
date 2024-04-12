/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.common.RequestCache.Key.keyFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.RequestCache;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RequestCacheAcceptanceTest {

  static CallCountingRequestRequestMatcher customMatcher = new CallCountingRequestRequestMatcher();

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(options().extensions(customMatcher).dynamicPort())
          .build();

  WireMockTestClient client;

  @BeforeEach
  public void init() {
    CallCountingRequestRequestMatcher.reset();
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  void cacheShouldEnsureSupplierIsOnlyCalledOnce() {
    wm.stubFor(get("/thing/1").andMatching("counting-matcher").willReturn(ok()));
    wm.stubFor(get("/thing/2").andMatching("counting-matcher").willReturn(ok()));

    WireMockResponse response = client.get("/thing/1");
    assertThat(response.statusCode(), is(404));

    assertThat(CallCountingRequestRequestMatcher.callCount, is(1));
  }

  @Test
  void cacheCanBeDisabled() {
    RequestCache.disable();
    wm.stubFor(get("/thing/1").andMatching("counting-matcher").willReturn(ok()));
    wm.stubFor(get("/thing/2").andMatching("counting-matcher").willReturn(ok()));

    client.get("/thing/1");

    assertThat(CallCountingRequestRequestMatcher.callCount, is(2));
  }

  public static class CallCountingRequestRequestMatcher extends RequestMatcherExtension {

    public static int callCount = 0;

    @Override
    public String getName() {
      return "counting-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      RequestCache.getCurrent()
          .get(
              keyFor(String.class, "myKey"),
              () -> {
                callCount++;
                return "value";
              });
      return MatchResult.partialMatch(0.1);
    }

    public static void reset() {
      callCount = 0;
    }
  }
}
