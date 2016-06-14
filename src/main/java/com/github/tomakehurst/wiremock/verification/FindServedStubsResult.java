package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;

import java.util.List;

public class FindServedStubsResult {

    private final List<ServedStub> servedStubs;

    @JsonCreator
    public FindServedStubsResult(@JsonProperty("servedStubs") List<ServedStub> servedStubs) {
        this.servedStubs = servedStubs;
    }

    public List<ServedStub> getServedStubs() {
        return servedStubs;
    }
}
