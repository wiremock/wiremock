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
package com.github.tomakehurst.wiremock.core;

import static com.github.tomakehurst.wiremock.core.Options.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.InetAddress;
import org.junit.jupiter.api.Test;

public class WireMockConfigurationTest {

  private static final String LOOPBACK_ADDRESS = InetAddress.getLoopbackAddress().getHostAddress();

  @Test
  public void testProxyPassThroughSetAsFalse() {
    Options config = wireMockConfig().proxyPassThrough(false);
    assertFalse(config.getStores().getSettingsStore().get().getProxyPassThrough());
  }

  @Test
  void setsMaxTemplateCacheEntries() {
    Options config = wireMockConfig().withMaxTemplateCacheEntries(11L);
    assertThat(config.getMaxTemplateCacheEntries(), is(11L));
  }

  @Test
  void maxTemplateCacheEntriesDefaultsWhenNotSpecified() {
    Options config = wireMockConfig();
    assertThat(config.getMaxTemplateCacheEntries(), is(DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES));
  }

  @Test
  void setsWebhookThreadpoolSize() {
    Options config = wireMockConfig().withWebhookThreadPoolSize(1000);
    assertThat(config.getWebhookThreadPoolSize(), is(1000));
  }

  @Test
  void webhookThreadpoolSizeWhenNotSpecified() {
    Options config = wireMockConfig();
    assertThat(config.getWebhookThreadPoolSize(), is(DEFAULT_WEBHOOK_THREADPOOL_SIZE));
  }

  @Test
  void bindAddressStartsAsDefault() {
    Options config = wireMockConfig();
    assertThat(config.bindAddress(), is(DEFAULT_BIND_ADDRESS));
    assertThat(DEFAULT_BIND_ADDRESS, is(not(LOOPBACK_ADDRESS)));
  }

  @Test
  void dynamicPortSetsBindAddress() {
    Options config = wireMockConfig().dynamicPort();
    assertThat(config.bindAddress(), is(LOOPBACK_ADDRESS));
  }

  @Test
  void dynamicPortDoesNotOverwriteBindAddress() {
    Options config = wireMockConfig().bindAddress("1.2.3.4").dynamicPort();
    assertThat(config.bindAddress(), is("1.2.3.4"));
  }

  @Test
  void bindAddressDoesOverwriteDynamicPort() {
    Options config = wireMockConfig().dynamicPort().bindAddress("1.2.3.4");
    assertThat(config.bindAddress(), is("1.2.3.4"));
  }

  @Test
  void dynamicHttpsPortSetsBindAddress() {
    Options config = wireMockConfig().dynamicHttpsPort();
    assertThat(config.bindAddress(), is(LOOPBACK_ADDRESS));
  }

  @Test
  void dynamicHttpsPortDoesNotOverwriteBindAddress() {
    Options config = wireMockConfig().bindAddress("1.2.3.4").dynamicHttpsPort();
    assertThat(config.bindAddress(), is("1.2.3.4"));
  }

  @Test
  void bindAddressDoesOverwriteDynamicHttpsPort() {
    Options config = wireMockConfig().dynamicHttpsPort().bindAddress("1.2.3.4");
    assertThat(config.bindAddress(), is("1.2.3.4"));
  }
}
