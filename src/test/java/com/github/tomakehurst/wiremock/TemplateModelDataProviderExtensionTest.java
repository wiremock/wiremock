/*
 * Copyright (C) 2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.TemplateModelDataProviderExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TemplateModelDataProviderExtensionTest {

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .templatingEnabled(true)
                  .globalTemplating(true)
                  .extensions(
                      new TemplateModelDataProviderExtension() {
                        @Override
                        public Map<String, Object> provideTemplateModelData(ServeEvent serveEvent) {
                          return Map.of(
                              "customData", Map.of("path", serveEvent.getRequest().getUrl()));
                        }

                        @Override
                        public String getName() {
                          return "custom-model-data";
                        }
                      }))
          .build();

  WireMockTestClient client;

  @BeforeEach
  void init() {
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  void appliesHelpersFromProvider() {
    wm.stubFor(get("/things").willReturn(ok("{{{ customData.path }}}")));

    WireMockResponse response = client.get("/things");

    assertThat(response.content(), is("/things"));
  }
}
