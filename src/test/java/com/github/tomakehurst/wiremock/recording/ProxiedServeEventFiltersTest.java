/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProxiedServeEventFiltersTest {
  @Test
  void applyWithUniversalRequestPattern() {
    ServeEvent serveEvent = proxiedServeEvent(mockRequest());
    ProxiedServeEventFilters filters =
        new ProxiedServeEventFilters(RequestPattern.ANYTHING, null, false);
    assertTrue(filters.test(serveEvent));

    // Should default to RequestPattern.ANYTHING when passing null for filters
    filters = new ProxiedServeEventFilters(null, null, false);
    assertTrue(filters.test(serveEvent));
  }

  @Test
  void applyWithUnproxiedServeEvent() {
    ServeEvent serveEvent = toServeEvent(null, null, ResponseDefinition.ok());
    ProxiedServeEventFilters filters = new ProxiedServeEventFilters(null, null, false);
    assertFalse(filters.test(serveEvent));
  }

  @Test
  void applyWithMethodPattern() {
    ProxiedServeEventFilters filters =
        new ProxiedServeEventFilters(newRequestPattern(GET, anyUrl()).build(), null, false);
    MockRequest request = mockRequest().method(GET).url("/foo");

    assertTrue(filters.test(proxiedServeEvent(request)));
    assertTrue(filters.test(proxiedServeEvent(request.url("/bar"))));
    assertFalse(filters.test(proxiedServeEvent(request.method(POST))));
  }

  @Test
  void applyWithIds() {
    List<UUID> ids =
        Arrays.asList(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            UUID.fromString("00000000-0000-0000-0000-000000000001"));
    ProxiedServeEventFilters filters = new ProxiedServeEventFilters(null, ids, false);

    assertTrue(filters.test(proxiedServeEvent(ids.get(0))));
    assertTrue(filters.test(proxiedServeEvent(ids.get(1))));
    assertFalse(
        filters.test(proxiedServeEvent(UUID.fromString("00000000-0000-0000-0000-000000000002"))));
  }

  @Test
  void applyWithMethodAndUrlPattern() {
    ProxiedServeEventFilters filters =
        new ProxiedServeEventFilters(
            newRequestPattern(GET, urlEqualTo("/foo")).build(), null, false);
    MockRequest request = mockRequest().method(GET).url("/foo");

    assertTrue(filters.test(proxiedServeEvent(request)));
    assertFalse(filters.test(proxiedServeEvent(request.url("/bar"))));
    assertFalse(filters.test(proxiedServeEvent(request.method(POST))));
  }

  @Test
  void applyWithIdsAndMethodPattern() {
    MockRequest request = mockRequest().method(GET).url("/foo");
    List<UUID> ids =
        Arrays.asList(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            UUID.fromString("00000000-0000-0000-0000-000000000001"));
    ProxiedServeEventFilters filters =
        new ProxiedServeEventFilters(newRequestPattern(GET, anyUrl()).build(), ids, false);

    assertTrue(filters.test(proxiedServeEvent(ids.get(0), request)));
    assertFalse(
        filters.test(
            proxiedServeEvent(UUID.fromString("00000000-0000-0000-0000-000000000002"), request)));
    assertFalse(filters.test(proxiedServeEvent(ids.get(0), request.method(POST))));
  }

  private ServeEvent toServeEvent(
      UUID id, MockRequest request, ResponseDefinition responseDefinition) {
    return new ServeEvent(
        id,
        request != null ? request.asLoggedRequest() : null,
        null,
        responseDefinition,
        null,
        true,
        Timing.UNTIMED);
  }

  private ServeEvent proxiedServeEvent(UUID id, MockRequest request) {
    return toServeEvent(
        id, request, new ResponseDefinitionBuilder().proxiedFrom("http://localhost").build());
  }

  private ServeEvent proxiedServeEvent(MockRequest request) {
    return proxiedServeEvent(null, request);
  }

  private ServeEvent proxiedServeEvent(UUID id) {
    return proxiedServeEvent(id, null);
  }
}
