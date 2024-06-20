/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class WebhooksAcceptanceTest {

  protected static final String WEBHOOK_REQUEST_SUB_EVENT_NAME = "WEBHOOK_REQUEST";
  protected static final String WEBHOOK_RESPONSE_SUB_EVENT_NAME = "WEBHOOK_RESPONSE";
  protected CountDownLatch latch;
  protected TestNotifier testNotifier = new TestNotifier();

  protected void assertSubEvent(SubEvent subEvent, String type, String message) {
    assertSubEvent(subEvent, type, Map.of("message", message));
  }

  protected void assertSubEvent(SubEvent subEvent, String type, Map<String, Object> data) {
    assertThat(subEvent, notNullValue());
    assertThat(subEvent.getType(), is(type));
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      boolean hasEntry =
          subEvent.getData().entrySet().stream()
              .anyMatch(
                  e ->
                      e.getKey().equals(entry.getKey())
                          && e.getValue().toString().contains(entry.getValue().toString()));
      assertTrue(hasEntry);
    }
  }

  protected void assertErrorMessage(String expectedErrorMessage) {
    List<String> errorMessages =
        await().until(() -> testNotifier.getErrorMessages(), hasSize(greaterThanOrEqualTo(1)));
    assertThat(errorMessages.get(0), is(expectedErrorMessage));
  }

  protected void waitForRequestToTargetServer() throws Exception {
    assertTrue(
        latch.await(20, SECONDS), "Timed out waiting for target server to receive a request");
  }

  protected void printAllInfoNotifications() {
    this.printAllNotifications("All info notifications", testNotifier.getInfoMessages());
  }

  protected void printAllErrorNotifications() {
    this.printAllNotifications("All error notifications", testNotifier.getErrorMessages());
  }

  private void printAllNotifications(String msg, List<String> notifications) {
    System.out.println(
        msg
            + ":\n"
            + notifications.stream()
                .map(message -> message.replace("\n", "\n>>> "))
                .collect(Collectors.joining("\n>>> ")));
  }
}
