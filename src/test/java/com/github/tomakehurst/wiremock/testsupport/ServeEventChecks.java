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
package com.github.tomakehurst.wiremock.testsupport;

import static com.github.tomakehurst.wiremock.common.Strings.normaliseLineBreaks;
import static com.github.tomakehurst.wiremock.stubbing.SubEvent.JSON_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Message;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.matching.MatchResult;

public class ServeEventChecks {

  public static void assertMessageSubEventPresent(Admin admin, String type, String message) {
    admin.getServeEvents().getServeEvents().stream()
        .findFirst()
        .ifPresentOrElse(
            serveEvent -> {
              assertThat(serveEvent.getSubEvents(), hasSize(1));
              serveEvent.getSubEvents().stream()
                  .findFirst()
                  .ifPresentOrElse(
                      subEvent -> {
                        assertThat(subEvent.getType(), is(type));
                        assertThat(
                            normaliseLineBreaks(subEvent.getDataAs(Message.class).getMessage()),
                            is(normaliseLineBreaks(message)));
                      },
                      () -> fail("No sub events found"));
            },
            () -> fail("No serve events found"));
  }

  public static void checkMessage(MatchResult matchResult, String type, String message) {
    matchResult.getSubEvents().stream()
        .filter(subEvent -> subEvent.getType().equals(type))
        .findFirst()
        .ifPresentOrElse(
            subEvent ->
                assertThat(
                    normaliseLineBreaks(subEvent.getDataAs(Message.class).getMessage()),
                    is(normaliseLineBreaks(message))),
            () -> fail("No sub event of type " + type + " found"));
  }

  public static void checkJsonError(MatchResult matchResult, String detailMessage) {
    matchResult.getSubEvents().stream()
        .filter(subEvent -> subEvent.getType().equals(JSON_ERROR))
        .findFirst()
        .ifPresentOrElse(
            subEvent ->
                assertThat(
                    normaliseLineBreaks(subEvent.getDataAs(Errors.class).first().getDetail()),
                    is(normaliseLineBreaks(detailMessage))),
            () -> fail("No sub event of type JSON_ERROR found"));
  }
}
