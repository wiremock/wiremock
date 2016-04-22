package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class NearMiss implements Comparable<NearMiss> {

    private final LoggedRequest request;
    private final StubMapping mapping;
    private final MatchResult matchResult;

    @JsonCreator
    public NearMiss(@JsonProperty("request") LoggedRequest request,
                    @JsonProperty("stubMapping") StubMapping mapping,
                    @JsonProperty("matchResult") MatchResult matchResult) {
        this.request = request;
        this.mapping = mapping;
        this.matchResult = matchResult;
    }

    public LoggedRequest getRequest() {
        return request;
    }

    public StubMapping getStubMapping() {
        return mapping;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    @Override
    public int compareTo(NearMiss o) {
        return o.getMatchResult().compareTo(matchResult);
    }
}
