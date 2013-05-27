package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

import java.util.List;

public class DisabledRequestJournal implements RequestJournal {

    @Override
    public int countRequestsMatching(RequestPattern requestPattern) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public void reset() {
    }

    @Override
    public void requestReceived(Request request) {
    }
}
