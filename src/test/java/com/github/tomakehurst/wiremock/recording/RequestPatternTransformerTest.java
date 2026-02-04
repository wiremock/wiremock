/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class RequestPatternTransformerTest {
  @Test
  public void applyIncludesMethodAndUrlMatchers() {
    Request request =
        mockRequest()
            .url("/foo")
            .method(RequestMethod.GET)
            .header("User-Agent", "foo")
            .header("X-Foo", "bar");

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/foo"));

    assertEquals(
        expected.build(), new RequestPatternTransformer(null, null).apply(request).build());
  }

  @Test
  public void applyIncludesQueryMethod() {
    Request request = mockRequest().url("/foo").method(RequestMethod.QUERY);

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.QUERY, urlEqualTo("/foo"));

    assertEquals(
        expected.build(), new RequestPatternTransformer(null, null).apply(request).build());
  }

  @Test
  public void applyWithHeaders() {
    Request request =
        mockRequest()
            .url("/")
            .method(RequestMethod.POST)
            .header("X-CaseSensitive", "foo")
            .header("X-Ignored", "ignored")
            .header("X-CaseInsensitive", "Baz");

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.POST, urlEqualTo("/"))
            .withHeader("X-CaseSensitive", equalTo("foo"))
            .withHeader("X-CaseInsensitive", equalToIgnoreCase("Baz"));

    Map<String, CaptureHeadersSpec> headers =
        Map.of(
            "X-CaseSensitive",
            new CaptureHeadersSpec(false),
            "X-CaseInsensitive",
            new CaptureHeadersSpec(true));

    assertEquals(
        expected.build(), new RequestPatternTransformer(headers, null).apply(request).build());
  }

  @Test
  public void applyWithCaptureAllHeaders() {
    Request request =
        mockRequest()
            .url("/")
            .method(RequestMethod.POST)
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain")
            .header("X-Custom", "value");

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.POST, urlEqualTo("/"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Accept", equalTo("text/plain"))
            .withHeader("X-Custom", equalTo("value"));

    assertEquals(
        expected.build(),
        new RequestPatternTransformer(null, true, null).apply(request).build());
  }

  @Test
  public void applyWithCaptureAllHeadersAndSpecificHeaderSettings() {
    Request request =
        mockRequest()
            .url("/")
            .method(RequestMethod.POST)
            .header("Content-Type", "application/json")
            .header("Accept", "TEXT/PLAIN")
            .header("X-Custom", "Value");

    Map<String, CaptureHeadersSpec> headers =
        Map.of("Accept", new CaptureHeadersSpec(true));

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.POST, urlEqualTo("/"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Accept", equalToIgnoreCase("TEXT/PLAIN"))
            .withHeader("X-Custom", equalTo("Value"));

    assertEquals(
        expected.build(),
        new RequestPatternTransformer(headers, true, null).apply(request).build());
  }

  @Test
  public void applyWithCaptureAllHeadersFalse() {
    Request request =
        mockRequest()
            .url("/")
            .method(RequestMethod.GET)
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain");

    RequestPatternBuilder expected =
        new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/"));

    assertEquals(
        expected.build(),
        new RequestPatternTransformer(null, false, null).apply(request).build());
  }
}
