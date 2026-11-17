/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.core;

import static com.github.tomakehurst.wiremock.core.Options.DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES;
import static com.github.tomakehurst.wiremock.core.Options.DEFAULT_WEBHOOK_THREADPOOL_SIZE;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.Test;

public class WireMockConfigurationTest {

  @Test
  public void testProxyPassThroughSetAsFalse() {
    WireMockConfiguration wireMockConfiguration =
        WireMockConfiguration.wireMockConfig().proxyPassThrough(false);
    assertFalse(wireMockConfiguration.getStores().getSettingsStore().get().getProxyPassThrough());
  }

  @Test
  public void maxRequestJournalEntriesIsEnforcedEvenWhenProxyPassThroughIsSetFirst() {
    WireMockConfiguration config =
        WireMockConfiguration.wireMockConfig().proxyPassThrough(false).maxRequestJournalEntries(2);

    addServeEvents(config, 3);

    assertThat(config.getStores().getRequestJournalStore().getAllKeys().count(), is(2L));
    assertFalse(config.getStores().getSettingsStore().get().getProxyPassThrough());
  }

  @Test
  public void maxRequestJournalEntriesIsEnforcedWhenProxyPassThroughIsSetAfter() {
    WireMockConfiguration config =
        WireMockConfiguration.wireMockConfig().maxRequestJournalEntries(2).proxyPassThrough(false);

    addServeEvents(config, 3);

    assertThat(config.getStores().getRequestJournalStore().getAllKeys().count(), is(2L));
    assertFalse(config.getStores().getSettingsStore().get().getProxyPassThrough());
  }

  @Test
  void setsMaxTemplateCacheEntries() {
    Options config = WireMockConfiguration.wireMockConfig().withMaxTemplateCacheEntries(11L);
    assertThat(config.getMaxTemplateCacheEntries(), is(11L));
  }

  @Test
  void maxTemplateCacheEntriesDefaultsWhenNotSpecified() {
    Options config = WireMockConfiguration.wireMockConfig();
    assertThat(config.getMaxTemplateCacheEntries(), is(DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES));
  }

  @Test
  void setsWebhookThreadpoolSize() {
    Options config = WireMockConfiguration.wireMockConfig().withWebhookThreadPoolSize(1000);
    assertThat(config.getWebhookThreadPoolSize(), is(1000));
  }

  @Test
  void webhookThreadpoolSizeWhenNotSpecified() {
    Options config = WireMockConfiguration.wireMockConfig();
    assertThat(config.getWebhookThreadPoolSize(), is(DEFAULT_WEBHOOK_THREADPOOL_SIZE));
  }

  private static void addServeEvents(Options config, int count) {
    for (int i = 0; i < count; i++) {
      config
          .getStores()
          .getRequestJournalStore()
          .add(ServeEvent.of(createFrom(mockRequest().url("/" + i))));
    }
  }
}
