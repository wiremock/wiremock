/*
 * Copyright (C) 2015-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class JettySettingsTest {

  private static final int number = 1234;
  private static final long longNumber = Long.MAX_VALUE;

  @Test
  public void testBuilderWithValues() {
    JettySettings.Builder builder = JettySettings.Builder.aJettySettings();
    builder
        .withAcceptors(number)
        .withAcceptQueueSize(number)
        .withRequestHeaderSize(number)
        .withResponseHeaderSize(number)
        .withStopTimeout(longNumber)
        .withIdleTimeout(longNumber);

    JettySettings jettySettings = builder.build();

    ensurePresent(jettySettings.getAcceptors());
    ensurePresent(jettySettings.getAcceptQueueSize());
    ensurePresent(jettySettings.getRequestHeaderSize());
    ensurePresent(jettySettings.getResponseHeaderSize());
    ensureLongPresent(jettySettings.getStopTimeout());
    ensureLongPresent(jettySettings.getIdleTimeout());
  }

  @Test
  public void testBuilderWithNoValues() {

    JettySettings.Builder builder = JettySettings.Builder.aJettySettings();
    JettySettings jettySettings = builder.build();

    assertFalse(jettySettings.getAcceptors().isPresent());
    assertFalse(jettySettings.getAcceptQueueSize().isPresent());
    assertFalse(jettySettings.getRequestHeaderSize().isPresent());
    assertFalse(jettySettings.getStopTimeout().isPresent());
    assertFalse(jettySettings.getIdleTimeout().isPresent());
  }

  private void ensurePresent(Optional<Integer> optional) {
    assertTrue(optional.isPresent());
    assertEquals(Integer.valueOf(number), optional.get());
  }

  private void ensureLongPresent(Optional<Long> optional) {
    assertTrue(optional.isPresent());
    assertEquals(Long.valueOf(longNumber), optional.get());
  }
}
