package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface StubMappingListener {

    void onStubMappingReset(StubMappingContext context);
    void onStubMappingAdded(StubMappingContext context, StubMapping added);
    void onStubMappingRemoved(StubMappingContext context, StubMapping removed);
    void onStubMappingUpdated(StubMappingContext context, StubMapping before, StubMapping after);
}
