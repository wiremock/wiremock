package com.github.tomakehurst.wiremock.verification.journal;

public final class RequestJournalFactory {

    private RequestJournalFactory() {}

    public static RequestJournal fromCapacity(Integer capacity) {
        if (capacity == null) {
            return new UnboundedInMemoryRequestJournal();
        } else if (capacity > 0) {
            return new BoundedInMemoryRequestJournal(capacity);
        } else if (capacity == 0) {
            return new EmptyRequestJornal();
        } else {
            throw new IllegalArgumentException();
        }
    }

}
