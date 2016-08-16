package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.admin.Paginator;

import java.util.List;

public abstract class RequestJournalDependentResult<T> extends PaginatedResult<T> {

    private final boolean requestJournalDisabled;

    protected RequestJournalDependentResult(Paginator<T> paginator, boolean requestJournalDisabled) {
        super(paginator);
        this.requestJournalDisabled = requestJournalDisabled;
    }

    protected RequestJournalDependentResult(List<T> source, Meta meta, boolean requestJournalDisabled) {
        super(source, meta);
        this.requestJournalDisabled = requestJournalDisabled;
    }

    public boolean isRequestJournalDisabled() {
        return requestJournalDisabled;
    }
}
