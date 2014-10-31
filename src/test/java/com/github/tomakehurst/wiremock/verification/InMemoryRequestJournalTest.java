package com.github.tomakehurst.wiremock.verification;

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
    /**
     * Test requests used in this test
     */
    private LoggedRequest request1, request2, request3;

    @Before
    public void createTestRequests() {
        Mockery context = new Mockery();
        request1 = createFrom(aRequest(context, "log1").withUrl("/logging1").build());
        request2 = createFrom(aRequest(context, "log2").withUrl("/logging2").build());
        request3 = createFrom(aRequest(context, "log3").withUrl("/logging3").build());
    }

    @Test
    public void testRequestsMatching() {
        RequestJournal journal = new InMemoryRequestJournal(null);

        // Add request 1 twice + request 2 and check if they are queried correctly
        journal.requestReceived(request1);
        journal.requestReceived(request1);
        journal.requestReceived(request2);

        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(2));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
    }

    @Test
    public void testResetMaxEntries() throws Exception {
        // When we reset the size of the journal is reset to the initial max length
        RotatingRequestJournal journal = new InMemoryRequestJournal(1);
        journal.setMaxEntries(2);
        assertThat(journal.getMaxEntries(), is(2));
        journal.reset();
        assertThat(journal.getMaxEntries(), is(1));
    }

    @Test
    public void testResetJournalContent() throws Exception {
        // When we reset the journal it is cleared
        Mockery context = new Mockery();
        LoggedRequest loggedRequest = createFrom(aRequest(context)
                .withUrl("/for/logging")
                .build());

        RotatingRequestJournal journal = new InMemoryRequestJournal(1);
        journal.requestReceived(loggedRequest);
        assertThat(journal.countRequestsMatching(everything()), is(1));
        journal.reset();
        assertThat(journal.countRequestsMatching(everything()), is(0));
    }

    @Test
    public void testJournalRotation() throws Exception {
        // When we add more entries to the journal than it can take old requests are removed
        RotatingRequestJournal journal = new InMemoryRequestJournal(2);
        // First add the first two requests and verify that they are there
        journal.requestReceived(request1);
        journal.requestReceived(request2);
        assertThat(journal.countRequestsMatching(everything()), is(2));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(1));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));

        // Then add third one and verify that only 2 and 3 are there
        journal.requestReceived(request3);
        assertOnlyLastTwoRequestsLeft(journal);
    }

    @Test
    public void testSetMaxEntriesCutsRequestLog() throws Exception {
        // When the request journal has three entries and no max requests and then we set the maximum number of requests#
        // the journal is cut
        RotatingRequestJournal journal = new InMemoryRequestJournal(10);
        // Add the requests
        journal.requestReceived(request1);
        journal.requestReceived(request2);
        journal.requestReceived(request3);

        // Cut the journal
        journal.setMaxEntries(2);
        assertOnlyLastTwoRequestsLeft(journal);
    }

    /**
     * Assert that only the entries with id 2 and 3 are left
     *
     * @param journal Journal to check
     */
    private void assertOnlyLastTwoRequestsLeft(RotatingRequestJournal journal) {
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging1")).build()), is(0));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging2")).build()), is(1));
        assertThat(journal.countRequestsMatching(getRequestedFor(urlEqualTo("/logging3")).build()), is(1));
    }
}