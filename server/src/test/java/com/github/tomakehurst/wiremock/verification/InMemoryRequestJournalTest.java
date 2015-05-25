package com.github.tomakehurst.wiremock.verification;

import com.google.common.base.Optional;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.everything;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InMemoryRequestJournalTest {

    private LoggedRequest request1, request2, request3;

    @Before
    public void createTestRequests() {
        Mockery context = new Mockery();
        request1 = createFrom(aRequest(context, "log1").withUrl("/logging1").build());
        request2 = createFrom(aRequest(context, "log2").withUrl("/logging2").build());
        request3 = createFrom(aRequest(context, "log3").withUrl("/logging3").build());
    }

    @Test
    public void returnsAllLoggedRequestsWhenNoJournalSizeLimit() {
        RequestJournal journal = new InMemoryRequestJournal(Optional.<Integer>absent());

        journal.requestReceived(request1);
        journal.requestReceived(request1);
        journal.requestReceived(request2);

        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(2));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
    }

    @Test
    public void resettingTheJournalClearsAllEntries() throws Exception {
        Mockery context = new Mockery();
        LoggedRequest loggedRequest = createFrom(aRequest(context)
                .withUrl("/for/logging")
                .build());

        RequestJournal journal = new InMemoryRequestJournal(Optional.of(1));
        journal.requestReceived(loggedRequest);
        assertThat(journal.countRequestsMatching(everything()), is(1));
        journal.reset();
        assertThat(journal.countRequestsMatching(everything()), is(0));
    }

    @Test
    public void discardsOldRequestsWhenJournalSizeIsLimited() throws Exception {
        RequestJournal journal = new InMemoryRequestJournal(Optional.of(2));

        journal.requestReceived(request1);
        journal.requestReceived(request2);

        assertThat(journal.countRequestsMatching(everything()), is(2));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(1));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));

        journal.requestReceived(request3);
        assertOnlyLastTwoRequestsLeft(journal);
    }

    private void assertOnlyLastTwoRequestsLeft(RequestJournal journal) {
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(0));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging3")).build()), is(1));
    }
}