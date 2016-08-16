package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Paginator;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;

import java.util.List;

public class GetServedStubsResult extends RequestJournalDependentResult<ServedStub> {

    @JsonCreator
    public GetServedStubsResult(@JsonProperty("servedStubs") List<ServedStub> source,
                                @JsonProperty("meta") Meta meta,
                                @JsonProperty("requestJournalDisabled") boolean requestJournalDisabled) {
        super(source, meta, requestJournalDisabled);
    }

    public GetServedStubsResult(Paginator<ServedStub> paginator, boolean requestJournalDisabled) {
        super(paginator, requestJournalDisabled);
    }

    public static GetServedStubsResult requestJournalEnabled(Paginator<ServedStub> paginator) {
        return new GetServedStubsResult(paginator, false);
    }

    public static GetServedStubsResult requestJournalDisabled(Paginator<ServedStub> paginator) {
        return new GetServedStubsResult(paginator, true);
    }

    public List<ServedStub> getServedStubs() {
        return select();
    }
}
