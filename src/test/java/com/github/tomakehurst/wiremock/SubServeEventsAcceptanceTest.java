/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.verification.diff.DiffEventData;
import org.junit.jupiter.api.Test;

public class SubServeEventsAcceptanceTest extends AcceptanceTestBase {

  // Diffs are saved as sub events
  @Test
  void nonMatchDiffsAreSavedAsSubEvents() {
    wm.stubFor(get("/right").willReturn(ok()));

    testClient.get("/wrong");

    ServeEvent serveEvent = wm.getAllServeEvents().get(0);
    SubEvent subEvent = serveEvent.getSubEvents().stream().findFirst().get();
    assertThat(subEvent.getType(), is("REQUEST_NOT_MATCHED"));
    assertThat(subEvent.getTimeOffsetNanos(), greaterThan(0L));
    assertThat(subEvent.getDataAs(DiffEventData.class).getReport(), containsString("/wrong"));
  }

  @Test
  void errorsDuringMatchingAreCapturedInSubEvents() {
    wm.stubFor(
        post("/json").withRequestBody(equalToJson("{ \"thing\": \"value\" }")).willReturn(ok()));

    testClient.postJson("/json", "{ \"thing\": ");

    ServeEvent serveEvent = wm.getAllServeEvents().get(0);
    SubEvent failedJsonParseWarning =
        serveEvent.getSubEvents().stream()
            .filter(sub -> sub.getType().equals(SubEvent.JSON_ERROR))
            .findFirst()
            .get();
    Errors.Error error =
        failedJsonParseWarning.getDataAs(Errors.class).getErrors().stream().findFirst().get();
    assertThat(
        error.getDetail(), containsString("Unexpected end-of-input within/between Object entries"));
  }

  @Test
  void onlyAppendsOneSubEventPerSpecificError() {
    wm.stubFor(post("/json").withRequestBody(equalToJson("{ \"thing\": 1 }")).willReturn(ok()));
    wm.stubFor(post("/json").withRequestBody(equalToJson("{ \"thing\": 2 }")).willReturn(ok()));
    wm.stubFor(post("/json").withRequestBody(equalToJson("{ \"thing\": 3 }")).willReturn(ok()));

    testClient.postXml("/json", "<whoops />");

    ServeEvent serveEvent = wm.getAllServeEvents().get(0);
    assertThat(
        serveEvent.getSubEvents().stream()
            .filter(sub -> sub.getType().equals(SubEvent.JSON_ERROR))
            .count(),
        is(1L));
  }
}
