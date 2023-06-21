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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.stubbing.ServeEventFactory.newPostMatchServeEvent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.InMemorySettingsStore;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

public class StubResponseRendererTest {
  private static final int TEST_TIMEOUT = 500;

  private BlobStore filesBlobStore;
  private SettingsStore settingsStore;
  private List<ResponseTransformer> responseTransformers;
  private StubResponseRenderer stubResponseRenderer;

  @BeforeEach
  public void init() {
    filesBlobStore = Mockito.mock(BlobStore.class);
    settingsStore = new InMemorySettingsStore();
    responseTransformers = new ArrayList<>();
    stubResponseRenderer =
        new StubResponseRenderer(filesBlobStore, settingsStore, null, responseTransformers);
  }

  @Test
  @Timeout(TEST_TIMEOUT)
  public void endpointFixedDelayShouldOverrideGlobalDelay() throws Exception {
    settingsStore.set(GlobalSettings.builder().fixedDelay(1000).build());

    Response response = stubResponseRenderer.render(createServeEvent(100));

    assertThat(response.getInitialDelay(), is(100L));
  }

  @Test
  @Timeout(TEST_TIMEOUT)
  public void globalFixedDelayShouldNotBeOverriddenIfNoEndpointDelaySpecified() throws Exception {
    settingsStore.set(GlobalSettings.builder().fixedDelay(1000).build());

    Response response = stubResponseRenderer.render(createServeEvent(null));

    assertThat(response.getInitialDelay(), is(1000L));
  }

  @Test
  @Timeout(TEST_TIMEOUT)
  public void shouldSetGlobalFixedDelayOnResponse() throws Exception {
    settingsStore.set(GlobalSettings.builder().fixedDelay(1000).build());

    Response response = stubResponseRenderer.render(createServeEvent(null));

    assertThat(response.getInitialDelay(), is(1000L));
  }

  @Test
  public void shouldSetEndpointFixedDelayOnResponse() throws Exception {
    Response response = stubResponseRenderer.render(createServeEvent(2000));

    assertThat(response.getInitialDelay(), is(2000L));
  }

  @Test
  @Timeout(TEST_TIMEOUT)
  public void shouldSetEndpointDistributionDelayOnResponse() throws Exception {
    settingsStore.set(GlobalSettings.builder().delayDistribution(() -> 123).build());

    Response response = stubResponseRenderer.render(createServeEvent(null));

    assertThat(response.getInitialDelay(), is(123L));
  }

  @Test
  @Timeout(TEST_TIMEOUT)
  public void shouldCombineFixedDelayDistributionDelay() throws Exception {
    settingsStore.set(GlobalSettings.builder().delayDistribution(() -> 123).build());
    Response response = stubResponseRenderer.render(createServeEvent(2000));
    assertThat(response.getInitialDelay(), is(2123L));
  }

  private ServeEvent createServeEvent(Integer fixedDelayMillis) {
    return newPostMatchServeEvent(
        mockRequest(),
        new ResponseDefinition(
            0,
            "",
            "",
            null,
            "",
            "",
            null,
            null,
            fixedDelayMillis,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true));
  }
}
