/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty12;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.github.tomakehurst.wiremock.jetty12.server.CustomHttpServerFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProgrammaticHttpServerAcceptanceTest {

  static TestNotifier notifier = new TestNotifier();

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .dynamicHttpsPort()
                  .httpServerFactory(new CustomHttpServerFactory())
                  .notifier(notifier))
          .build();

  @BeforeAll
  static void init() {
    notifier.reset();
  }

  @Test
  public void correctServerLoaded() {
    assertThat(notifier.getInfoMessages(), hasItem("Using HTTP server impl: CustomHttpServer"));
  }
}
