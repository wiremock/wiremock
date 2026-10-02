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
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.admin.model.ListChannelProvidersResult;
import com.github.tomakehurst.wiremock.admin.model.SingleChannelProviderResult;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.message.channel.ChannelProvider;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void returnsEmptyListWhenNoProvidersRegistered() {
    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "channelProviders": [],
              "meta": { "total": 0 }
            }
            """));
  }

  @Test
  void returnsSingleProviderAfterRegistration() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "channelProviders": [
                {
                  "name": "test-provider-alpha",
                  "driverType": "in-memory",
                  "settings": {}
                }
              ],
              "meta": { "total": 1 }
            }
            """));
  }

  @Test
  void returnsMultipleProviders() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));
    registerChannelProvider(channelProvider().named(PROVIDER_BETA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), jsonPartEquals("meta.total", 2));
    assertThat(
        response.content(), jsonPartEquals("channelProviders[0].name", "\"test-provider-alpha\""));
    assertThat(
        response.content(), jsonPartEquals("channelProviders[1].name", "\"test-provider-beta\""));
  }

  @Test
  void providerWithSettingsIsIncludedInResponse() {
    registerChannelProvider(
        channelProvider()
            .named(PROVIDER_ALPHA)
            .withDriver("in-memory")
            .withSetting("timeout", 5000));

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "channelProviders": [
                {
                  "name": "test-provider-alpha",
                  "driverType": "in-memory",
                  "settings": {
                    "timeout": 5000
                  }
                }
              ],
              "meta": { "total": 1 }
            }
            """));
  }

  @Test
  void providerRemovedFromListAfterDeletion() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));
    registerChannelProvider(channelProvider().named(PROVIDER_BETA).withDriver("in-memory"));

    removeChannelProvider(PROVIDER_ALPHA);

    WireMockResponse response = testClient.get("/__admin/channel-providers");

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "channelProviders": [
                {
                  "name": "test-provider-beta",
                  "driverType": "in-memory",
                  "settings": {}
                }
              ],
              "meta": { "total": 1 }
            }
            """));
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
  void getChannelProviderByNameViaApiReturnsProviderJson() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response = testClient.get("/__admin/channel-providers/" + PROVIDER_ALPHA);

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "name": "test-provider-alpha",
              "driverType": "in-memory",
              "settings": {}
            }
            """));
  }

  @Test
  void getChannelProviderByNameViaApiReturns404WhenNotFound() {
    WireMockResponse response = testClient.get("/__admin/channel-providers/nonexistent");

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void getChannelProviderByNameViaApiIncludesSettings() {
    registerChannelProvider(
        channelProvider()
            .named(PROVIDER_ALPHA)
            .withDriver("in-memory")
            .withSetting("timeout", 3000)
            .withSetting("retries", 5));

    WireMockResponse response = testClient.get("/__admin/channel-providers/" + PROVIDER_ALPHA);

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "name": "test-provider-alpha",
              "driverType": "in-memory",
              "settings": {
                "timeout": 3000,
                "retries": 5
              }
            }
            """));
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

  // --- update (rename) channel provider ---

  @Test
  void renameChannelProviderViaApi() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response =
        testClient.putWithBody(
            "/__admin/channel-providers/" + PROVIDER_ALPHA,
            // language=json
            """
            {
              "name": "renamed-provider",
              "driverType": "in-memory",
              "settings": {}
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(200));
    assertThat(
        response.content(),
        jsonEquals(
            // language=json
            """
            {
              "name": "renamed-provider",
              "driverType": "in-memory",
              "settings": {}
            }
            """));
    safeRemoveChannelProvider("renamed-provider");
  }

  @Test
  void renamedProviderAppearsUnderNewNameInList() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    testClient.putWithBody(
        "/__admin/channel-providers/" + PROVIDER_ALPHA,
        // language=json
        """
        {
          "name": "renamed-provider",
          "driverType": "in-memory",
          "settings": {}
        }
        """,
        "application/json");

    WireMockResponse listResponse = testClient.get("/__admin/channel-providers");
    assertThat(
        listResponse.content(), jsonPartEquals("channelProviders[0].name", "\"renamed-provider\""));
    assertThat(getChannelProvider(PROVIDER_ALPHA).isPresent(), is(false));
    safeRemoveChannelProvider("renamed-provider");
  }

  @Test
  void renamingNonExistentProviderReturns404() {
    WireMockResponse response =
        testClient.putWithBody(
            "/__admin/channel-providers/nonexistent",
            // language=json
            """
            {
              "name": "new-name",
              "driverType": "in-memory",
              "settings": {}
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void changingDriverTypeIsRejectedViaApi() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response =
        testClient.putWithBody(
            "/__admin/channel-providers/" + PROVIDER_ALPHA,
            // language=json
            """
            {
              "name": "test-provider-alpha",
              "driverType": "kafka",
              "settings": {}
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
  }

  @Test
  void changingSettingsIsRejectedViaApi() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    WireMockResponse response =
        testClient.putWithBody(
            "/__admin/channel-providers/" + PROVIDER_ALPHA,
            // language=json
            """
            {
              "name": "test-provider-alpha",
              "driverType": "in-memory",
              "settings": {
                "timeout": 1000
              }
            }
            """,
            "application/json");

    assertThat(response.statusCode(), is(422));
  }

  @Test
  void renameChannelProviderViaDsl() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    ChannelProvider updated =
        updateChannelProvider(
            PROVIDER_ALPHA, channelProvider().named("renamed-provider").withDriver("in-memory"));

    assertThat(updated.getName(), is("renamed-provider"));
    assertThat(updated.getDriverType(), is("in-memory"));
    assertThat(getChannelProvider(PROVIDER_ALPHA).isPresent(), is(false));
    assertThat(getChannelProvider("renamed-provider").isPresent(), is(true));
    safeRemoveChannelProvider("renamed-provider");
  }

  @Test
  void changingDriverTypeIsRejectedViaDsl() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    assertThrows(
        InvalidInputException.class,
        () ->
            updateChannelProvider(
                PROVIDER_ALPHA, channelProvider().named(PROVIDER_ALPHA).withDriver("kafka")));
  }

  @Test
  void changingSettingsIsRejectedViaDsl() {
    registerChannelProvider(channelProvider().named(PROVIDER_ALPHA).withDriver("in-memory"));

    assertThrows(
        InvalidInputException.class,
        () ->
            updateChannelProvider(
                PROVIDER_ALPHA,
                channelProvider()
                    .named(PROVIDER_ALPHA)
                    .withDriver("in-memory")
                    .withSetting("k", "v")));
  }
}
