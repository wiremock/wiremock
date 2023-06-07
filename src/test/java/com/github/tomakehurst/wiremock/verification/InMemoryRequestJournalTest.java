/*
 * Copyright (C) 2014-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestMatcherExtension.ALWAYS;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.everything;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryRequestJournalTest {

  static final Map<String, RequestMatcherExtension> NO_CUSTOM_MATCHERS = Collections.emptyMap();

  private ServeEvent serveEvent1, serveEvent2, serveEvent3;

  @BeforeEach
  public void createTestRequests() {
    serveEvent1 = ServeEvent.of(createFrom(aRequest("log1").withUrl("/logging1").build()));
    serveEvent2 = ServeEvent.of(createFrom(aRequest("log2").withUrl("/logging2").build()));
    serveEvent3 = ServeEvent.of(createFrom(aRequest("log3").withUrl("/logging3").build()));
  }

  @Test
  public void returnsAllLoggedRequestsWhenNoJournalSizeLimit() {
    RequestJournal journal = new InMemoryRequestJournal(null, NO_CUSTOM_MATCHERS);

    journal.requestReceived(serveEvent1);
    journal.requestReceived(serveEvent1);
    journal.requestReceived(serveEvent2);

    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(2));
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
  }

  @Test
  public void resettingTheJournalClearsAllEntries() throws Exception {
    LoggedRequest loggedRequest = createFrom(aRequest().withUrl("/for/logging").build());

    RequestJournal journal = new InMemoryRequestJournal(1, NO_CUSTOM_MATCHERS);
    journal.requestReceived(ServeEvent.of(loggedRequest));
    assertThat(journal.countRequestsMatching(everything()), is(1));
    journal.reset();
    assertThat(journal.countRequestsMatching(everything()), is(0));
  }

  @Test
  public void discardsOldRequestsWhenJournalSizeIsLimited() throws Exception {
    RequestJournal journal = new InMemoryRequestJournal(2, NO_CUSTOM_MATCHERS);

    journal.requestReceived(serveEvent1);
    journal.requestReceived(serveEvent2);

    assertThat(journal.countRequestsMatching(everything()), is(2));
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(1));
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));

    journal.requestReceived(serveEvent3);
    assertOnlyLastTwoRequestsLeft(journal);
  }

  @Test
  public void matchesRequestWithCustomMatcherDefinition() throws Exception {
    RequestJournal journal =
        new InMemoryRequestJournal(null, Map.of(ALWAYS.getName(), ALWAYS));

    journal.requestReceived(serveEvent1);
    journal.requestReceived(serveEvent2);

    assertThat(
        journal.countRequestsMatching(requestMadeFor(ALWAYS.getName(), Parameters.empty()).build()),
        is(2));
    assertThat(
        journal.countRequestsMatching(requestMadeFor("not-existing", Parameters.empty()).build()),
        is(0));

    assertThat(
        journal
            .getRequestsMatching(requestMadeFor(ALWAYS.getName(), Parameters.empty()).build())
            .size(),
        is(2));
    assertThat(
        journal
            .getRequestsMatching(requestMadeFor("not-existing", Parameters.empty()).build())
            .size(),
        is(0));
  }

  private void assertOnlyLastTwoRequestsLeft(RequestJournal journal) {
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(0));
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
    assertThat(
        journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging3")).build()), is(1));
  }
}
