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
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.admin.model.ListChannelProvidersResult;
import com.github.tomakehurst.wiremock.admin.model.SingleChannelProviderResult;
import com.github.tomakehurst.wiremock.message.channel.ChannelProvider;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class ChannelProviderApiAcceptanceTest extends AcceptanceTestBase {

  private static final String PROVIDER_ALPHA = "test-provider-alpha";
  private static final String PROVIDER_BETA = "test-provider-beta";

  @BeforeEach
  void cleanupProviders() {
    safeRemoveChannelProvider(PROVIDER_ALPHA);
    safeRemoveChannelProvider(PROVIDER_BETA);
  }

  @AfterEach
  void tearDown() {
    safeRemoveChannelProvider(PROVIDER_ALPHA);
    safeRemoveChannelProvider(PROVIDER_BETA);
  }

  private static void safeRemoveChannelProvider(String name) {
    try {
      removeChannelProvider(name);
    } catch (Exception ignored) {
    }
  }

  @Test
  void returnsEmptyListWhenNoProvidersRegistered() throws Exception {
    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{\"channelProviders\": [], \"meta\": {\"total\": 0}}", response.content(), true);
  }

  @Test
  void returnsSingleProviderAfterRegistration() throws Exception {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{"
            + "\"channelProviders\": [{"
            + "  \"name\": \"test-provider-alpha\","
            + "  \"driverType\": \"in-memory\","
            + "  \"settings\": {}"
            + "}],"
            + "\"meta\": {\"total\": 1}"
            + "}",
        response.content(),
        true);
  }

  @Test
  void returnsMultipleProvidersInRegistrationOrder() throws Exception {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));
    registerChannelProvider(channelProvider().named(PROVIDER_BETA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{"
            + "\"channelProviders\": ["
            + "  {\"name\": \"test-provider-alpha\", \"driverType\": \"in-memory\"},"
            + "  {\"name\": \"test-provider-beta\", \"driverType\": \"in-memory\"}"
            + "],"
            + "\"meta\": {\"total\": 2}"
            + "}",
        response.content(),
        false);
  }

  @Test
  void providerWithSettingsIsIncludedInResponse() throws Exception {
    registerChannelProvider(
        channelProvider()
            .named(PROVIDER_ALPHA)
            .withDriver("in-memory")
            .withSetting("timeout", 5000));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{"
            + "\"channelProviders\": [{"
            + "  \"name\": \"test-provider-alpha\","
            + "  \"driverType\": \"in-memory\","
            + "  \"settings\": {\"timeout\": 5000}"
            + "}],"
            + "\"meta\": {\"total\": 1}"
            + "}",
        response.content(),
        true);
  }

  @Test
  void providerRemovedFromListAfterDeletion() throws Exception {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));
    registerChannelProvider(channelProvider().named(PROVIDER_BETA).withDriver("in-memory"));

    removeChannelProvider(PROVIDER_ALPHA);

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{"
            + "\"channelProviders\": [{"
            + "  \"name\": \"test-provider-beta\","
            + "  \"driverType\": \"in-memory\","
            + "  \"settings\": {}"
            + "}],"
            + "\"meta\": {\"total\": 1}"
            + "}",
        response.content(),
        true);
  }

  @Test
  void listChannelProvidersViaDsl() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));
    registerChannelProvider(channelProvider().named(PROVIDER_BETA).withDriver("in-memory"));

    ListChannelProvidersResult result = listAllChannelProviders();

    assertThat(result.getMeta().total, is(2));
    List<ChannelProvider> providers = result.getChannelProviders();
    assertThat(providers, hasSize(2));
    assertThat(
        providers.stream().map(ChannelProvider::getName).toList(),
        containsInAnyOrder(PROVIDER_ALPHA, PROVIDER_BETA));
  }

  @Test
  void listChannelProvidersViaDslReturnsEmptyWhenNoneRegistered() {
    ListChannelProvidersResult result = listAllChannelProviders();

    assertThat(result.getMeta().total, is(0));
    assertThat(result.getChannelProviders(), empty());
  }

  @Test
  void listChannelProvidersViaDslReflectsProviderDriverType() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    ListChannelProvidersResult result = listAllChannelProviders();

    ChannelProvider provider = result.getChannelProviders().get(0);
    assertThat(provider.getName(), is(PROVIDER_ALPHA));
    assertThat(provider.getDriverType(), is("in-memory"));
  }

  // --- get single channel provider ---

  @Test
  void getChannelProviderByNameViaApiReturnsProviderJson() throws Exception {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers/" + PROVIDER_ALPHA);

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{\"name\": \"test-provider-alpha\", \"driverType\": \"in-memory\", \"settings\": {}}",
        response.content(),
        true);
  }

  @Test
  void getChannelProviderByNameViaApiReturns404WhenNotFound() {
    WireMockResponse response = testClient.get("/__admin/channel-providers/nonexistent");

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void getChannelProviderByNameViaApiIncludesSettings() throws Exception {
    registerChannelProvider(
        channelProvider()
            .named(PROVIDER_ALPHA)
            .withDriver("in-memory")
            .withSetting("timeout", 3000)
            .withSetting("retries", 5));

    WireMockResponse response = testClient.get("/__admin/channel-providers/" + PROVIDER_ALPHA);

    assertThat(response.statusCode(), is(200));
    JSONAssert.assertEquals(
        "{\"name\": \"test-provider-alpha\", \"driverType\": \"in-memory\","
            + " \"settings\": {\"timeout\": 3000, \"retries\": 5}}",
        response.content(),
        true);
  }

  @Test
  void getChannelProviderViaDslReturnsPresentResultForExistingProvider() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    SingleChannelProviderResult result = getChannelProvider(PROVIDER_ALPHA);

    assertThat(result.isPresent(), is(true));
    assertThat(result.getItem().getName(), is(PROVIDER_ALPHA));
    assertThat(result.getItem().getDriverType(), is("in-memory"));
  }

  @Test
  void getChannelProviderViaDslReturnsNotPresentResultForUnknownName() {
    SingleChannelProviderResult result = getChannelProvider("does-not-exist");

    assertThat(result.isPresent(), is(false));
  }
}
