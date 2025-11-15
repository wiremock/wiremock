/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import java.util.Optional;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

public class JUnitJupiterExtensionResetOnEachTestTest {

  CloseableHttpClient client;
  ExtensionContext extensionContext;

  @BeforeEach
  void init() {
    client = HttpClientFactory.createClient();

    extensionContext = Mockito.mock(ExtensionContext.class);
    when(extensionContext.getElement()).thenReturn(Optional.empty());
  }

  @Test
  void default_behavior_is_to_reset_stubs_in_before_each() throws Exception {
    WireMockExtension extension = WireMockExtension.newInstance().build();

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(0));

    extension.stubFor(get("/one").willReturn(ok()));
    assertThat(extension.getStubMappings(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(0));

    extension.stubFor(get("/two").willReturn(ok()));
    assertThat(extension.getStubMappings(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(0));
  }

  @Test
  void default_behavior_is_to_reset_requests_in_before_each() throws Exception {
    WireMockExtension extension = WireMockExtension.newInstance().build();

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(0));

    extension.stubFor(get("/one").willReturn(ok()));
    try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/one")))) {
      assertThat(response.getCode(), is(200));
    }
    assertThat(extension.getAllServeEvents(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(0));

    extension.stubFor(get("/two").willReturn(ok()));
    try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/two")))) {
      assertThat(response.getCode(), is(200));
    }
    assertThat(extension.getAllServeEvents(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(0));
  }

  @Test
  void can_configure_to_not_reset_stubs_in_before_each() throws Exception {
    WireMockExtension extension = WireMockExtension.newInstance().resetOnEachTest(false).build();

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(0));

    extension.stubFor(get("/one").willReturn(ok()));
    assertThat(extension.getStubMappings(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(1));

    extension.stubFor(get("/two").willReturn(ok()));
    assertThat(extension.getStubMappings(), hasSize(2));

    extension.beforeEach(extensionContext);
    assertThat(extension.getStubMappings(), hasSize(2));
  }

  @Test
  void can_configure_to_not_reset_requests_in_before_each() throws Exception {
    WireMockExtension extension = WireMockExtension.newInstance().resetOnEachTest(false).build();

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(0));

    extension.stubFor(get("/one").willReturn(ok()));
    try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/one")))) {
      assertThat(response.getCode(), is(200));
    }
    assertThat(extension.getAllServeEvents(), hasSize(1));

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(1));

    extension.stubFor(get("/two").willReturn(ok()));
    try (CloseableHttpResponse response = client.execute(new HttpGet(extension.url("/two")))) {
      assertThat(response.getCode(), is(200));
    }
    assertThat(extension.getAllServeEvents(), hasSize(2));

    extension.beforeEach(extensionContext);
    assertThat(extension.getAllServeEvents(), hasSize(2));
  }
}
